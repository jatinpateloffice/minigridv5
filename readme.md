### ACam
Child of CamActivity. Used to record videos.
- Keys
  - project id
  - scene id
  - show project progress [def=true]
- Incoming
  - self: redirect from learning **AVideoPlayback**
  - **ASceneEdit**: upon deleting all clips in the scene
  - **ASelfiePlayback**: upon retake of just recorded selfie video
  - **AProjectEdit**::FProjectTellScene: re record selfie video 
    - key-show project progress = false
- Outgoing
    - self: redirect from learning **AVideoPlayback**
    - **AVideoPlayer**: play leaning video, play recorded video after muxing clips
    - **ASelfiePlayback**: playback selfie video
    
### AClipCam
Child of CamActivity. Used to re-record clips in non selfie scene
- Keys
    - project id
    - scene id
    - clip file name
- Incoming
    - **ASceneEdit**: re-record selected clip of the scene
- Outgoing
    - **ASceneEdit**: upon back press, close press, clip re-recording complete success 
    
  