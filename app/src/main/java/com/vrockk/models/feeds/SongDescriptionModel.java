package com.vrockk.models.feeds;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SongDescriptionModel implements Serializable {

	@SerializedName("artist")
	private String artist;

	@SerializedName("name")
	private String name;

	public SongDescriptionModel(String artist, String name) {
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