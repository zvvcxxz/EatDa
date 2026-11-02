package com.sp.app.admin.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.sp.app.admin.domain.dto.AdminNoticeDto;
import com.sp.app.admin.mapper.AdminNoticeMapper;


import java.io.IOException;
import java.nio.file.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(transactionManager = "oracleTransactionManager", rollbackFor = Exception.class)
public class AdminNoticeServiceImpl implements AdminNoticeService {
    private final AdminNoticeMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(AdminNoticeServiceImpl.class);
    private final Path root;
    private final List<Path> roots;

    public AdminNoticeServiceImpl(AdminNoticeMapper mapper, @Value("${notice.upload-dir:${user.home}/eatda/uploads/notice}") String directory,
            @Value("${file.upload-root:./uploads}") String oldDirectory, ServletContext servletContext) {
        this.mapper = mapper;
        root = Path.of(directory).toAbsolutePath().normalize();
        Set<Path> candidates = new LinkedHashSet<>();
        candidates.add(root);
        candidates.add(Path.of(oldDirectory, "notice").toAbsolutePath().normalize());
        candidates.add(Path.of("uploads", "notice").toAbsolutePath().normalize());
        String webRoot = servletContext.getRealPath("/");
        if (webRoot != null) candidates.add(Path.of(webRoot, "uploads", "notice").toAbsolutePath().normalize());
        roots = List.copyOf(candidates);
    }


    private Path resolve(Path directory, String name) {
        if (name == null || name.isBlank() || name.contains("/") || name.contains("\\")
                || name.equals(".") || name.equals("..") || Path.of(name).isAbsolute()) {
            throw new IllegalArgumentException("잘못된 첨부파일 경로입니다.");
        }
        return directory.resolve(name).normalize();
    }

    private String originalName(MultipartFile file) {
        String name = Objects.toString(file.getOriginalFilename(), "attachment").replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "");
        return name.isBlank() ? "attachment" : name;
    }

    private String saveUploadedFile(MultipartFile file) throws IOException {
        Files.createDirectories(root);
        String name = UUID.randomUUID().toString();
        Path target = resolve(root, name);
        // DB 작업이 롤백되면 먼저 저장한 파일도 정리한다.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) removeQuietly(target);
                }
            });
        }
        try (var input = file.getInputStream()) {
            Files.copy(input, target);
        } catch (IOException | RuntimeException e) {
            removeQuietly(target);
            throw e;
        }
        return name;
    }

    private Path findStoredFile(String name) {
        for (Path directory : roots) {
            Path file = resolve(directory, name);
            if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) return file;
        }
        return null;
    }

    private void deleteAfterCommit(String name) {
        List<Path> files = roots.stream().map(directory -> resolve(directory, name)).toList();
        Runnable remove = () -> files.forEach(this::removeQuietly);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { remove.run(); }
            });
        } else remove.run();
    }

    private void removeQuietly(Path file) {
        try { Files.deleteIfExists(file); }
        catch (IOException e) { log.warn("공지 첨부파일 정리 실패: {}", file, e); }
    }

    @Override
    public void downloadFile(String savedName, String originalName, HttpServletResponse response) throws IOException {
        Path file = findStoredFile(savedName);
        if (file == null) { response.sendError(404, "첨부파일이 존재하지 않습니다."); return; }
        String encoded = URLEncoder.encode(Objects.toString(originalName, "attachment"), StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store");
        response.setContentLengthLong(Files.size(file));
        try (var input = Files.newInputStream(file)) { input.transferTo(response.getOutputStream()); }
    }

    private void validate(AdminNoticeDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank() || dto.getContent() == null || dto.getContent().isBlank())
            throw new IllegalArgumentException("제목과 내용을 입력해주세요.");
        if (dto.getStatus() != 0 && dto.getStatus() != 1) throw new IllegalArgumentException("공개 여부를 확인해주세요.");
        dto.setTitle(dto.getTitle().trim());
        dto.setPinnedYn("Y".equals(dto.getPinnedYn()) ? "Y" : "N");
    }
    private void upload(AdminNoticeDto dto) throws Exception {
        if (dto.getSelectFile() == null) return;
        for (MultipartFile file : dto.getSelectFile()) {
            if (file == null || file.isEmpty()) continue;
            dto.setOriginalFileName(originalName(file));
            dto.setFilePath(saveUploadedFile(file));
            mapper.insertNoticeFile(dto);
        }
    }
    @Override public void insertNotice(AdminNoticeDto dto, String ignored) throws Exception {
        validate(dto); mapper.insertNotice(dto); upload(dto);
    }
    @Override public void updateNotice(AdminNoticeDto dto, String ignored) throws Exception {
        validate(dto);
        if (mapper.findById(dto.getNoticeId()) == null) throw new IllegalArgumentException("공지사항이 존재하지 않습니다.");
        mapper.updateNotice(dto); upload(dto);
    }
    @Override public void deleteNotice(long id, String ignored) throws Exception {
        List<AdminNoticeDto> attached = mapper.listNoticeFile(id);
        mapper.deleteNoticeFile(Map.of("field", "NOTICE_ID", "noticeId", id));
        mapper.deleteNotice(id);
        for (AdminNoticeDto file : attached) deleteAfterCommit(file.getFilePath());
    }
    @Override public boolean deleteNoticeFile(long id, String ignored) throws Exception {
        AdminNoticeDto file = mapper.findByFileId(id);
        if (file == null) return false;
        mapper.deleteNoticeFile(Map.of("field", "NOTICE_FILE_ID", "noticeId", id));
        deleteAfterCommit(file.getFilePath());
        return true;
    }
    @Override public int dataCount(Map<String,Object> map) { return mapper.dataCount(map); }
    @Override public List<AdminNoticeDto> listNoticeTop() { return mapper.listNoticeTop(); }
    @Override public List<AdminNoticeDto> listNotice(Map<String,Object> map) { return mapper.listNotice(map); }
    @Override public AdminNoticeDto findById(long id) { return mapper.findById(id); }
    @Override public void updateHitCount(long id) throws Exception { mapper.updateHitCount(id); }
    @Override public AdminNoticeDto findByPrev(Map<String,Object> map) { return mapper.findByPrev(map); }
    @Override public AdminNoticeDto findByNext(Map<String,Object> map) { return mapper.findByNext(map); }
    @Override public List<AdminNoticeDto> listNoticeFile(long id) { return mapper.listNoticeFile(id); }
    @Override public AdminNoticeDto findByFileId(long id) { return mapper.findByFileId(id); }
}
