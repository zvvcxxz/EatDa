package com.sp.app.admin.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sp.app.admin.domain.dto.AdminTagDto;
import com.sp.app.admin.mapper.AdminTagMapper;

@Service
public class AdminTagServiceImpl implements AdminTagService{
	
	@Autowired
	private AdminTagMapper mapper;

	@Override
	public void insertTag(AdminTagDto dto) throws Exception {
		try {
			mapper.insertTag(dto);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void updateTag(AdminTagDto dto) throws Exception {
		try {
			mapper.updateTag(dto);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void deleteTag(long tagId) throws Exception {
		try {
            mapper.deleteTag(tagId);
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
	public List<AdminTagDto> listTag(Map<String, Object> map) {
		return mapper.listTag(map);
	}

	@Override
	public AdminTagDto findById(long tagId) {
		return mapper.findById(tagId);
	}

	@Override
	public AdminTagDto findByName(String tagName) {
		return mapper.findByName(tagName);
	}
}
