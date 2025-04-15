package com.example.media;

import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ServiceComponent;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.internal.GeneratedEntryPoint;

@OriginatingElement(
    topLevelClass = MediaService.class
)
@GeneratedEntryPoint
@InstallIn(ServiceComponent.class)
public interface MediaService_GeneratedInjector {
  void injectMediaService(MediaService mediaService);
}
