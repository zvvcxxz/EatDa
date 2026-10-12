package com.sp.app.admin.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sp.app.admin.domain.dto.AdminNoticeDto;
import com.sp.app.admin.mapper.AdminNoticeMapper;
import com.sp.app.common.FileManager;

@Service
public class AdminNoticeServiceImpl implements AdminNoticeService {

    @Autowired
    private AdminNoticeMapper mapper;
    
    @Autowired
    private FileManager fileManager;

    @Override
    public void insertNotice(AdminNoticeDto dto, String pathname) throws Exception {
        try {
            mapper.insertNotice(dto);
            
            if (!fileManager.isDirectoryExist(pathname)) {
                fileManager.createAllDirectories(pathname);
            }
            
            if (dto.getSelectFile() != null && !dto.getSelectFile().isEmpty()) {
                for (MultipartFile mf : dto.getSelectFile()) {
                    if (mf.isEmpty()) continue;
                    
                    String originalFilename = mf.getOriginalFilename();
                    String extension = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
                    
                    String saveFilename = fileManager.generateUniqueFileName(pathname, extension);
                    
                    mf.transferTo(new File(pathname, saveFilename));
                    
                    dto.setOriginalFileName(originalFilename);
                    dto.setFilePath(saveFilename);
                    
                    mapper.insertNoticeFile(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void updateNotice(AdminNoticeDto dto, String pathname) throws Exception {
        try {
            mapper.updateNotice(dto);
            
            if (dto.getSelectFile() != null && !dto.getSelectFile().isEmpty()) {
                if (!fileManager.isDirectoryExist(pathname)) {
                    fileManager.createAllDirectories(pathname);
                }
                
                for (MultipartFile mf : dto.getSelectFile()) {
                    if (mf.isEmpty()) continue;
                    
                    String originalFilename = mf.getOriginalFilename();
                    String extension = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
                    String saveFilename = fileManager.generateUniqueFileName(pathname, extension);
                    
                    mf.transferTo(new File(pathname, saveFilename));
                    
                    dto.setOriginalFileName(originalFilename);
                    dto.setFilePath(saveFilename);
                    mapper.insertNoticeFile(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void deleteNotice(long noticeId, String pathname) throws Exception {
        try {
            List<AdminNoticeDto> listFile = mapper.listNoticeFile(noticeId);
            if (listFile != null) {
                for (AdminNoticeDto dto : listFile) {
                    Path filePath = Paths.get(pathname, dto.getFilePath());
                    Files.deleteIfExists(filePath);
                }
            }
            
            Map<String, Object> map = new HashMap<>();
            map.put("field", "NOTICE_ID");
            map.put("noticeId", noticeId);
            mapper.deleteNoticeFile(map);
            
            mapper.deleteNotice(noticeId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public int dataCount(Map<String, Object> map) {
        return mapper.dataCount(map);
    }

    @Override
    public List<AdminNoticeDto> listNoticeTop() {
        return mapper.listNoticeTop();
    }

    @Override
    public List<AdminNoticeDto> listNotice(Map<String, Object> map) {
        return mapper.listNotice(map);
    }

    @Override
    public AdminNoticeDto findById(long noticeId) {
        return mapper.findById(noticeId);
    }

    @Override
    public void updateHitCount(long noticeId) throws Exception {
        try {
            mapper.updateHitCount(noticeId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public AdminNoticeDto findByPrev(Map<String, Object> map) {
        return mapper.findByPrev(map);
    }

    @Override
    public AdminNoticeDto findByNext(Map<String, Object> map) {
        return mapper.findByNext(map);
    }

    @Override
    public List<AdminNoticeDto> listNoticeFile(long noticeId) {
        return mapper.listNoticeFile(noticeId);
    }

    @Override
    public AdminNoticeDto findByFileId(long noticeFileId) {
        return mapper.findByFileId(noticeFileId);
    }

    @Override
    public boolean deleteNoticeFile(long noticeFileId, String pathname) throws Exception {
        boolean b = false;
        try {
            AdminNoticeDto dto = mapper.findByFileId(noticeFileId);
            if (dto != null) {
                Path filePath = Paths.get(pathname, dto.getFilePath());
                Files.deleteIfExists(filePath);
                
                Map<String, Object> map = new HashMap<>();
                map.put("field", "NOTICE_FILE_ID");
                map.put("noticeId", noticeFileId);
                mapper.deleteNoticeFile(map);
                
                b = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return b;
    }
}