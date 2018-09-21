package com.fortex.xRingDC.plugin.model;

import java.util.List;

public class Subscriber {
	public enum ENUM_GIVEUPBY {
		CLIENT, ACCT;
		public static ENUM_GIVEUPBY toValue(String groupBy) {
			switch(groupBy.toUpperCase()){
			case "CLIENT":
				return CLIENT;
			case "ACCT":
				return ACCT;
			default:
				return CLIENT;
			}
		}
	};
	private String subscriberName;
	private String userName;
	private boolean isLpLeg;
	private boolean isClientLeg;
	private boolean isClientLegChangeSide;	
	private List<String> filterByAcct;
	private List<String> filterByDomain;
	private List<String> filterByLP;
	private List<String> filterByPB;
	private String giveupBy = "PB";
	private String giveupAs = "";
	
	public List<String> getFilterByDomain() {
		return filterByDomain;
	}
	public void setFilterByDomain(List<String> filterByDomain) {
		this.filterByDomain = filterByDomain;
	}
	public List<String> getFilterByLP() {
		return filterByLP;
	}
	public void setFilterByLP(List<String> filterByLP) {
		this.filterByLP = filterByLP;
	}
	public List<String> getFilterByPB() {
		return filterByPB;
	}
	public void setFilterByPB(List<String> filterByPB) {
		this.filterByPB = filterByPB;
	}
	public String getSubscriberName() {
		return subscriberName;
	}
	public void setSubscriberName(String subscriberName) {
		this.subscriberName = subscriberName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public boolean isLpLeg() {
		return isLpLeg;
	}
	public void setLpLeg(boolean isLpLeg) {
		this.isLpLeg = isLpLeg;
	}
	public boolean isClientLeg() {
		return isClientLeg;
	}
	public void setClientLeg(boolean isClientLeg) {
		this.isClientLeg = isClientLeg;
	}
	public boolean isClientLegChangeSide() {
		return isClientLegChangeSide;
	}
	public void setClientLegChangeSide(boolean isClientLegChangeSide) {
		this.isClientLegChangeSide = isClientLegChangeSide;
	}
	public List<String> getFilterByAcct() {
		return filterByAcct;
	}
	public void setFilterByAcct(List<String> filterByAcct) {
		this.filterByAcct = filterByAcct;
	}
	public void setGiveupBy(String giveupBy) {
		this.giveupBy = giveupBy;
	}
	public String getGiveupBy(){
		return giveupBy;
	}
	public String getGiveupAs(){
		return giveupAs;
	}
	public void setGiveupAs(String giveupAs){
		this.giveupAs = giveupAs;
	}
}
