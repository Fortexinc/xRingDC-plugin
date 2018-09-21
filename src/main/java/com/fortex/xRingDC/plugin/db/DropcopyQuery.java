package com.fortex.xRingDC.plugin.db;

public class DropcopyQuery {
	public static final String LPLEG = "CLIENTVSLP";
	public static final String CLIENTLEG = "LPVSCLIENT";
	public static final int AssetTypeSPOT = 3;
	public static final int AssetTypeCFD = 7;
	public static final String query = 
			"SELECT PBLEG, seqno, Client, ExeBroker, ReportType, AssetType, Side, Symbol, TradeQuantity, TradePrice, price2, SystemOrderID, Domain, primebroker, "
			+ " clearacct, Account, RefAcct, GiveupAsAcct, Route, TradeTime, TradeDate, ExeRefID, Login, Other, ExecDate, FutSettDate, DollarValue, ClOrderID, fillseqno, seqno2 "
			+ " FROM ReportsTradeCapture "
			+ " WHERE seqno2 >= ? "
			+ " ORDER BY ABS(seqno), PBLEG, seqno2 "; 			
}
