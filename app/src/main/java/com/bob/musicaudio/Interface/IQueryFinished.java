/**
 * Copyright (c) www.longdw.com
 */
package com.bob.musicaudio.Interface;

import com.bob.musicaudio.model.MusicInfo;

import java.util.List;


public interface IQueryFinished {
	
	public void onFinished(List<MusicInfo> list);

}
