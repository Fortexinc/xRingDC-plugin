package com.fortex.xRingDC.plugin.model;
/*
 * 				String seqNoStr = rs.getString("seqno");				
				String client = rs.getString("Client");
				String lp = rs.getString("ExeBroker");
				String reportType = rs.getString("ReportType");
				int assetType = rs.getInt("assetType");
				String side =  rs.getString("Side");
				String symbol = rs.getString("Symbol");
				String tradeQuantity = rs.getString("TradeQuantity");
				String tradePrice = rs.getString("TradePrice");
				//String conversionRate = rs.getString("price2");
				String systemOrderID = rs.getString("SystemOrderID");				
				String domain = rs.getString("Domain");				
				String pb = rs.getString("primebroker");
				//String clearAcct = rs.getString("clearacct");
				//String account = rs.getString("Account");
				String refAcct = rs.getString("RefAcct");
				//String giveupAsAcct = rs.getString("GiveupAsAcct");
				//String route = rs.getString("Route");
				String tradeTime = rs.getString("TradeTime");
				//String tradeDate = rs.getString("TradeDate");
				String exeRefID = rs.getString("ExeRefID");
				//String login = rs.getString("Login");
				//String other = rs.getString("Other");
				String execDate = rs.getString("ExecDate");
				String valueDate = rs.getString("FutSettDate");
				//String dollarValue = rs.getString("DollarValue");
				//String clOrderID = rs.getString("ClOrderID");
				//String fillseqno = rs.getString("fillseqno");
				String seqNo2Str = rs.getString("seqno2");
 */
public class ReportsTradeCapture {
	private String pbleg;
	private String seqno;
	private String client;
	private String exeBroker;
	private String reportType;
	private int assetType;
	private String side;
	private String symbol;
	private String tradeQuantity;
	private String tradePrice;
	private String systemOrderID;
	private String domain;
	private String primebroker;
	private String refAcct;
	private String tradeTime;
	private String exeRefID;
	private String execDate;
	private String futSettDate;
	private String seqno2;
	
	public String getPbleg() {
		return pbleg;
	}
	public void setPbleg(String pbleg) {
		this.pbleg = pbleg;
	}
	public String getSeqno() {
		return seqno;
	}
	public void setSeqno(String seqno) {
		this.seqno = seqno;
	}
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}
	public String getExeBroker() {
		return exeBroker;
	}
	public void setExeBroker(String exeBroker) {
		this.exeBroker = exeBroker;
	}
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	public int getAssetType() {
		return assetType;
	}
	public void setAssetType(int assetType) {
		this.assetType = assetType;
	}
	public String getSide() {
		return side;
	}
	public void setSide(String side) {
		this.side = side;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getTradeQuantity() {
		return tradeQuantity;
	}
	public void setTradeQuantity(String tradeQuantity) {
		this.tradeQuantity = tradeQuantity;
	}
	public String getTradePrice() {
		return tradePrice;
	}
	public void setTradePrice(String tradePrice) {
		this.tradePrice = tradePrice;
	}
	public String getSystemOrderID() {
		return systemOrderID;
	}
	public void setSystemOrderID(String systemOrderID) {
		this.systemOrderID = systemOrderID;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getPrimebroker() {
		return primebroker;
	}
	public void setPrimebroker(String primebroker) {
		this.primebroker = primebroker;
	}
	public String getRefAcct() {
		return refAcct;
	}
	public void setRefAcct(String refAcct) {
		this.refAcct = refAcct;
	}
	public String getTradeTime() {
		return tradeTime;
	}
	public void setTradeTime(String tradeTime) {
		this.tradeTime = tradeTime;
	}
	public String getExeRefID() {
		return exeRefID;
	}
	public void setExeRefID(String exeRefID) {
		this.exeRefID = exeRefID;
	}
	public String getExecDate() {
		return execDate;
	}
	public void setExecDate(String execDate) {
		this.execDate = execDate;
	}
	
	public String getFutSettDate() {
		return futSettDate;
	}
	public void setFutSettDate(String futSettDate) {
		this.futSettDate = futSettDate;
	}
	public String getSeqno2() {
		return seqno2;
	}
	public void setSeqno2(String seqno2) {
		this.seqno2 = seqno2;
	}	
}
