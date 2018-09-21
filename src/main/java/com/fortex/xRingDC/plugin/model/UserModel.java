package com.fortex.xRingDC.plugin.model;

import java.util.Date;

public class UserModel {
	
	private Integer domain;
	private Integer quoteService;
	private String userName;
	private String targetId;
	private String passwordHash;
	private String ip;
	private Date logonTime;
	private int metals;
	private int cfd;
	
	public int getMetals() {
		return metals;
	}
	public void setMetals(int metals) {
		this.metals = metals;
	}
	public int getCfd() {
		return cfd;
	}
	public void setCfd(int cfd) {
		this.cfd = cfd;
	}
	public UserModel(String userName) {
		this.userName = userName;
	}
	public UserModel(Integer domain, Integer quoteService, String userName) {
		this.domain = domain;
		this.quoteService = quoteService;
		this.userName = userName;
	}
	public Integer getDomain() {
		return domain;
	}
	public void setDomain(Integer domain) {
		this.domain = domain;
	}
	public Integer getQuoteService() {
		return quoteService;
	}
	public void setQuoteService(Integer quoteService) {
		this.quoteService = quoteService;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getTargetId() {
		return targetId;
	}
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Date getLogonTime() {
		return logonTime;
	}
	public void setLogonTime(Date logonTime) {
		this.logonTime = logonTime;
	}
}
