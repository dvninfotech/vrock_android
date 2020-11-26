package com.vrockk.models.songs_list;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class SongsListModel{

	@SerializedName("total")
	private int total;

	@SerializedName("data")
	private List<DataItem> data;

	@SerializedName("success")
	private boolean success;

	@SerializedName("message")
	private String message;

	public int getTotal(){
		return total;
	}

	public List<DataItem> getData(){
		return data;
	}

	public boolean isSuccess(){
		return success;
	}

	public String getMessage(){
		return message;
	}
}