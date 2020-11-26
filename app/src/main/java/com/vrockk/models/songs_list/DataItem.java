package com.vrockk.models.songs_list;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DataItem  implements Serializable {
	@SerializedName("originalName")
	private String originalName;

	@SerializedName("song")
	private String song;

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("isDeleted")
	private boolean isDeleted;

	@SerializedName("size")
	private int size;

	@SerializedName("__v")
	private int V;

	@SerializedName("description")
	private Description description;

	@SerializedName("_id")
	private String id;

	@SerializedName("type")
	private String type;

	@SerializedName("status")
	private int status;

	@SerializedName("uploadedBy")
	private String uploadedBy;

	@SerializedName("updatedAt")
	private String updatedAt;

	public DataItem(String originalName, String song, String createdAt, boolean isDeleted, int size,
					int v, Description description, String id, String type, int status, String uploadedBy,
					String updatedAt) {
		this.originalName = originalName;
		this.song = song;
		this.createdAt = createdAt;
		this.isDeleted = isDeleted;
		this.size = size;
		V = v;
		this.description = description;
		this.id = id;
		this.type = type;
		this.status = status;
		this.uploadedBy = uploadedBy;
		this.updatedAt = updatedAt;
	}

	public String getOriginalName(){
		return originalName;
	}

	public String getSong(){
		return song;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public String getUploadedBy(){
		return uploadedBy;
	}

	public boolean isIsDeleted(){
		return isDeleted;
	}

	public int getSize(){
		return size;
	}

	public int getV(){
		return V;
	}

	public Description getDescription(){
		return description;
	}

	public String getId(){
		return id;
	}

	public String getType(){
		return type;
	}

	public String setUploadedBy(){
		return uploadedBy;
	}

	public int getStatus(){
		return status;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}
}