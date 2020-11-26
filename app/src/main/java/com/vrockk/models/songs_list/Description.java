package com.vrockk.models.songs_list;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Description  implements Serializable {

	@SerializedName("artist")
	private String artist;

	@SerializedName("name")
	private String name;

	public Description(String artist, String name) {
		this.artist = artist;
		this.name = name;
	}

	public String getArtist(){
		return artist;
	}

	public String getName(){
		return name;
	}
}