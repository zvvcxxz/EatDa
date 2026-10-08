package com.sp.app.admin.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminHomeMapper {
	public int memberCount();
	public int recipeCount();
	public int pendingReportCount();
	public int pendingInquiryCount();
}
