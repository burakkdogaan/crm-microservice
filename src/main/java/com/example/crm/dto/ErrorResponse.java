package com.example.crm.dto;

public class ErrorResponse {
	private final String message;
	private final String source;
	//private final int i18nid;
	// excel sayfası açılacak - hata kodu ve karşılığında açıklaması olucak.
	// frontend tarafına verilir. json olarak verilmesi daha iyi.
	
	
	public ErrorResponse(String message, String source) {
		this.message = message;
		this.source = source;
	}
	public String getMessage() {
		return message;
	}
	public String getSource() {
		return source;
	}
	
}
