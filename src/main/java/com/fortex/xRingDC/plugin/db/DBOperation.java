package com.fortex.xRingDC.plugin.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;

import com.fortex.lib.globalservices.GlobalRuntime;
import com.fortex.xRingDC.plugin.model.ReportsTradeCapture;
import com.fortex.xRingDC.plugin.model.UserModel;
import com.fortex.xRingDC.plugin.utils.ConfigSetting;


public class DBOperation {
	protected static BasicDataSource dataSource;
	static {
		dataSource = new BasicDataSource();		
		dataSource.setDriverClassName(ConfigSetting.getDatabaseProperty("driverClass"));
		dataSource.setUrl(ConfigSetting.getDatabaseProperty("url"));
		dataSource.setUsername(ConfigSetting.getDatabaseProperty("username"));
		dataSource.setPassword(ConfigSetting.getDatabaseProperty("password"));				
		dataSource.setValidationQuery(ConfigSetting.getDatabaseProperty("validationQuery"));		
		dataSource.setInitialSize(Integer.parseInt(ConfigSetting.getDatabaseProperty("initialSize")));
		dataSource.setMaxTotal(Integer.parseInt(ConfigSetting.getDatabaseProperty("maxTotal")));
		dataSource.setMaxWaitMillis(Integer.parseInt(ConfigSetting.getDatabaseProperty("maxWaitMillis")));
		dataSource.setRemoveAbandonedOnBorrow(true);
		dataSource.setRemoveAbandonedTimeout(Integer.parseInt(ConfigSetting.getDatabaseProperty("removeAbandonedTimeout")));
		dataSource.setPoolPreparedStatements(true);
	}
	
	/**
	 * 
	 * <p>Description:Check if the account exists and the password match</p> 
	 *
	 * @author Patrick Chi
	 * @date 2016-08-05 
	 * @param username
	 * @param password
	 * @return
	 * @throws SQLException
	 */
	public static UserModel getUserInfo(String userName, int[] loginType) throws SQLException {
		Connection conn = dataSource.getConnection();
		String hashUserName= GlobalRuntime.hashPwd(userName);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		UserModel user = null;
		try{
			StringBuffer sb = new StringBuffer();			 
			sb = new StringBuffer();
			sb.append("SELECT TargetID, PasswordHash, domain,quoteService,la.cfd,la.metals FROM loginTable lt, loginaccountinfo la,systemconfigs sc WHERE ")
			.append(" lt.LoginHash = ? AND lt.loginType in(?,?) AND")
			.append(" lt.domain != sc.DomainClosedAccts AND")
			.append(" lt.login = la.Account");
			
			stmt = conn.prepareStatement(sb.toString());
			stmt.setString(1, hashUserName);
			stmt.setInt(2, loginType[0]);
			stmt.setInt(3, loginType[1]);
			rs = stmt.executeQuery();
			if (rs.next()) {
				user = new UserModel(rs.getInt("domain"), rs.getInt("quoteService"), userName);
				user.setTargetId(rs.getString("TargetID"));
				user.setPasswordHash(rs.getString("PasswordHash"));
				user.setCfd(rs.getInt("cfd"));
				user.setMetals(rs.getInt("metals"));
			}
		}finally{
			close(rs,stmt,conn);
		}
		return user;
	}

	
	/**
	 * <p>Description:close ResultSet,Statement and Connection.</p>
	 * @author Ivan Huo
	 * @date 2016-08-25	
	 * @return
	 * @throws SQLException
	 */
	private static void close(ResultSet rs,Statement stmt,Connection conn) {
		if(rs != null)
			try {rs.close();} catch (SQLException e) {}
		if(stmt != null)
			try {stmt.close();} catch (SQLException e) {}
		if(conn != null)
			try {conn.close();} catch (SQLException e) {}
	}
	
	
	public static List<ReportsTradeCapture> getReportsTradeCapture(int seqNo, boolean deleteAckedMsgOnly) throws SQLException{
		
		int currentSeqNo = 0;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		CallableStatement proc = null;
		Connection conn = null;
		List<ReportsTradeCapture> result = new ArrayList<ReportsTradeCapture>();
		String sqlProc = "{call sp_process_TradeCapture()}";
		
		ArrayList<String> seqNoList = new ArrayList<String>();
		try {
			conn = dataSource.getConnection();
			proc = conn.prepareCall(sqlProc);
			proc.execute();
			proc.close();
			pstmt = conn.prepareStatement(DropcopyQuery.query);
			pstmt.setInt(1, seqNo);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				ReportsTradeCapture report = new ReportsTradeCapture();
				report.setPbleg(rs.getString("pbleg"));
				report.setSeqno(rs.getString("seqno"));
				report.setClient(rs.getString("client"));
				report.setExeBroker(rs.getString("exeBroker"));
				report.setReportType(rs.getString("reportType"));
				report.setAssetType(rs.getInt("assetType"));
				report.setSide(rs.getString("side"));
				report.setSymbol(rs.getString("symbol"));
				report.setTradeQuantity(rs.getString("tradeQuantity"));
				report.setTradePrice(rs.getString("tradePrice"));
				report.setSystemOrderID(rs.getString("systemOrderID"));
				report.setDomain(rs.getString("domain"));
				report.setPrimebroker(rs.getString("primebroker"));
				report.setRefAcct(rs.getString("refAcct"));
				report.setTradeTime(rs.getString("tradeTime"));
				report.setExeRefID(rs.getString("exeRefID"));
				report.setExecDate(rs.getString("execDate"));
				report.setFutSettDate(rs.getString("futSettDate"));
				report.setSeqno2(rs.getString("seqno2"));
				seqNoList.add(report.getSeqno2());
				result.add(report);
				if (Integer.parseInt(report.getSeqno2()) > currentSeqNo) 
					currentSeqNo = Integer.parseInt(report.getSeqno2());
			}
			
			pstmt.close();
			pstmt = conn.prepareStatement("delete from ReportsTradeCapture where seqno2 <= ?" + (!deleteAckedMsgOnly ? "" : " and status = 'CONFIRMED';"));
			pstmt.setInt(1, currentSeqNo);
			pstmt.executeUpdate();
		} finally {
			if (!seqNoList.isEmpty())
				Logger.getLogger("Event").info("The seqno " + seqNoList + " were retrieved from database.");
			close(rs, pstmt, conn);
		}
		return result;
	}
		
	public static String getTargetIDByUserName(String userName) throws SQLException {
		String sql = "SELECT targetID FROM logintable WHERE login = ?";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String targetId = null;
		
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userName);
			rs = pstmt.executeQuery();
			if (rs.next())
				targetId = rs.getString("targetId");
			
		} finally {
			close(rs, pstmt, conn);
		}
		return targetId;
	}
}

