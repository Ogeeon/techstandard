package ru.techstandard.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.techstandard.client.model.Constants;
import word.utils.Utils;

public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
    {
		int BUFFER = 1024 * 100;
		resp.setContentType( "application/octet-stream" );
		ServletOutputStream outputStream = resp.getOutputStream();
		String type = req.getParameter("type");
		if (type.equals("attachment")) {

//			String fileName = req.getParameter("fileName");
			String fileName="";
//			resp.setHeader( "Content-Disposition", "attachment;filename=" + "\"" + fileName + "\"" );
			int attachId = Integer.valueOf(req.getParameter("attachId"));
//			System.out.println("fileName="+fileName);
//			System.out.println("attachId="+attachId);

			Connection conn = DBConnect.getConnection();
			String savedFilename = "";
			try {
				PreparedStatement ps = conn.prepareStatement("SELECT saved_as, filename FROM attachments WHERE id=" + attachId);
				ResultSet result = ps.executeQuery();
				result.first();
				savedFilename = result.getString(1);
				fileName = result.getString(2);
				result.close();
				ps.close();
			} catch (SQLException sqle) {
				System.out.println("doGet getsavedFilename exception: "+sqle.getMessage());
			}

			resp.setHeader( "Content-Disposition", "attachment;filename=" + "\"" + fileName + "\"" );
			
			if (savedFilename == null || savedFilename.isEmpty())
				throw new ServletException("saved filename not found");

			File f = new File(savedFilename.replace("\\", "\\\\"));
			FileInputStream is = new FileInputStream(f);

			resp.setContentLength( Long.valueOf( f.length() ).intValue() );
			resp.setBufferSize( BUFFER );
			//Your IO code goes here to create a file and set to outputStream//
			BufferedInputStream fif = new BufferedInputStream(is);
			int data;
			while((data = fif.read()) != -1) {
				outputStream.write(data);
			}
			fif.close();
			outputStream.close();
			return;
		} else if (type.equals("contract")) {
			resp.setContentType("text/doc; charset=utf-8");
			
//			String xmlTemplate = Utils.readFile(System.getProperty("user.home") + File.separator +  "templatecontract.doc");
			String xmlTemplate = Utils.readFile("/usr/local/" + "templatecontract.doc");
			resp.setHeader( "Content-Disposition", "attachment;filename=\"dogovor.doc\"" );
			
			Connection conn = DBConnect.getConnection();
			SimpleDateFormat signedFormat = new SimpleDateFormat("«dd» MMMM YYYY г.", myDateFormatSymbols);
			SimpleDateFormat dueFormat = new SimpleDateFormat("dd MMMM YYYY г.", myDateFormatSymbols);
			
			String id = req.getParameter("id");
			try {
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM templates INNER JOIN clients ON templates.client_id = clients.id WHERE templates.id=?");
				ps.setString(1, id);
				ResultSet result = ps.executeQuery();
				result.first();
				xmlTemplate = replacePh(xmlTemplate, "phNum", result.getString("num"));
				xmlTemplate = replacePh(xmlTemplate, "phDate", signedFormat.format(result.getDate("signed")));
				xmlTemplate = replacePh(xmlTemplate, "phFullName", result.getString("full_name"));
				xmlTemplate = replacePh(xmlTemplate, "phSigner", result.getString("signer"));
				xmlTemplate = replacePh(xmlTemplate, "phFoundation", result.getString("foundation"));
				xmlTemplate = replacePh(xmlTemplate, "phSubject", result.getString("subject").replace("\n", Constants.LINEBREAK));
				xmlTemplate = replacePh(xmlTemplate, "phDuration", result.getString("duration"));
				xmlTemplate = replacePh(xmlTemplate, "phPrePay", result.getString("prepay"));
				xmlTemplate = replacePh(xmlTemplate, "phLeftPay", String.valueOf(100 - result.getInt("prepay")));
				
				boolean multiple = result.getBoolean("multiple_items");
				if (multiple) {
					String unitName = result.getString("unit_name");
					Double unitPrice = result.getDouble("unit_price");
					String unitPriceStr = String.format("%,.2f", unitPrice) + " (" + Money2str.moneytostr(unitPrice) + ")";
					Double nds = 0.18*(unitPrice/1.18);
					String ndsStr = " в том числе НДС (18%) " + String.format("%,.2f", nds) + " руб. (" + Money2str.moneytostr(nds) + ".)";
					Double totalPrice = result.getDouble("total_price");
					String totalPriceStr = String.format("%,.2f", totalPrice) + " (" + Money2str.moneytostr(totalPrice) + ")";
					String priceStr = "Стоимость работ за 1 единицу " + unitName + " составляет " + unitPriceStr + ndsStr + " Стоимость работ итого: " + totalPriceStr; 
					xmlTemplate = replacePh(xmlTemplate, "phPrice", priceStr);
				} else {
					Double totalPrice = result.getDouble("total_price");
					String totalPriceStr = String.format("%,.2f", totalPrice) + " (" + Money2str.moneytostr(totalPrice) + ")";
					Double nds = 0.18*(totalPrice/1.18);
					String ndsStr = " в том числе НДС (18%) " + String.format("%,.2f", nds) + " руб. (" + Money2str.moneytostr(nds) + ".)";
					String priceStr = "Стоимость работ по договору составляет " + totalPriceStr + ndsStr; 
					xmlTemplate = replacePh(xmlTemplate, "phPrice", priceStr);
				}
				
				xmlTemplate = replacePh(xmlTemplate, "phDueDate", dueFormat.format(result.getDate("due_date")));
				xmlTemplate = replacePh(xmlTemplate, "phAddress2", result.getString("address2"));
				xmlTemplate = replacePh(xmlTemplate, "phAddress", result.getString("address"));
				xmlTemplate = replacePh(xmlTemplate, "phPhone", result.getString("phone"));
				xmlTemplate = replacePh(xmlTemplate, "phFax", result.getString("fax"));
				xmlTemplate = replacePh(xmlTemplate, "phEmail", result.getString("email"));
				xmlTemplate = replacePh(xmlTemplate, "phBank", result.getString("bank_name"));
				xmlTemplate = replacePh(xmlTemplate, "phInn", result.getString("inn"));
				xmlTemplate = replacePh(xmlTemplate, "phKpp", result.getString("kpp"));
				xmlTemplate = replacePh(xmlTemplate, "phRsch", result.getString("rsch"));
				xmlTemplate = replacePh(xmlTemplate, "phKsch", result.getString("ksch"));
				xmlTemplate = replacePh(xmlTemplate, "phOkpo", result.getString("okpo"));
				xmlTemplate = replacePh(xmlTemplate, "phOkato", result.getString("okato"));
				xmlTemplate = replacePh(xmlTemplate, "phOgrn", result.getString("ogrn"));
				xmlTemplate = replacePh(xmlTemplate, "phBoss", result.getString("boss"));
				
				
				result.close();
				ps.close();
			} catch (SQLException sqle) {
				System.out.println("get template data exception: "+sqle.getMessage());
			}
			// отобразили данные - можно удалять заготовку
			try {
				PreparedStatement ps = conn.prepareStatement("DELETE FROM templates WHERE id=?");
				ps.setString(1, id);
				ps.executeUpdate();
				ps.close();
			} catch (SQLException sqle) {
				System.out.println("delete template exception: "+sqle.getMessage());
			}
			
			
			outputStream.write(xmlTemplate.getBytes());
			outputStream.close();
			
			return;
		} else {
			throw new ServletException("Запрос не распознан");
		}
    }

	private String replacePh(String base, String placeHolder, String value) {
		if (value == null)
			value = "";
		if(!base.contains(placeHolder)) {
			//don't want to use log now because I want to keep it simple...
			System.out.println("### WARN: couldn't find the place holder: " + placeHolder);
			return base;
		}
		return base.replace(placeHolder, value);
	}
	
	private static DateFormatSymbols myDateFormatSymbols = new DateFormatSymbols(){
		private static final long serialVersionUID = 1L;

		@Override
        public String[] getMonths() {
            return new String[]{"января", "февраля", "марта", "апреля", "мая", "июня",
                "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }
        
    };
}