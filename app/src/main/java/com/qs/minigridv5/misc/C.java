package com.qs.minigridv5.misc;

public class C {

    public static final String T = "TAAG";

    public static final short  LANG_ENGLISH = 0;
    public static final short  LANG_HINDI   = 1;
    public static final String LANG_API     = "en";

    public static final String DEVELOPER_KEY = "AIzaSyB0zWBLX_uoKsRtP4qUY3bNaO9A-NtShDo";

    public static final int NAV_COMMUNITY_IDX = 0;
    public static final int NAV_LEARN_IDX     = 1;
    public static final int NAV_MAKE_IDX      = 2;
    public static final int NAV_LIBRARY_IDX   = 3;

    public static final int    MOVIE_STATUS_LOCAL         = 1;
    public static final int    MOVIE_STATUS_UPLOADED      = 2;
    public static final int    MOVIE_STATUS_PUBLISHED     = 3;
    public static final int    MOVIE_STATUS_UNPUBLISHED   = 4;
    public static final String NO_YOUTUBE_LINK_VALUE      = "nayt";
    public static final String NO_REMOTE_VIDEO_PATH_VALUE = "nrvp";

    public static final int MOVIE_UPLOADED_RESPONSE              = 0;// not approved by admin, unseen by master
    public static final int MOVIE_AWAITING_RESPONSE              = 3;// approved by admin, unseen by master
    public static final int MOVIE_PUBLISHED_RESPONSE             = 1;// approved by both
    public static final int MOVIE_UNPUBLISHED_BY_MASTER_RESPONSE = 4;// disapproved by master
    public static final int MOVIE_UNPUBLISHED_BY_ADMIN_RESPONSE  = 2;// disapproved by admin

    public static final  String VIDEO_UPLOAD_HASH_KEY   = "68zG1sbr0sro";
    private static final String API_DOMAIN              = "https://www.minigridstories.com";
    private static final String API_STRING              = API_DOMAIN + "/api/v1";
    public static final  String API_COMPANY_LIST_URL    = API_STRING + "/minigrids-company-list.php";
    public static final  String API_COUNTRY_LIST_URL    = API_STRING + "/minigrids-country-list.php";
    public static final  String API_STATE_LIST_URL      = API_STRING + "/minigrids-state-list.php?country_id=1";
    public static final  String API_VIDEO_UPLOAD_URL    = API_STRING + "/minigrids-videouploadapi.php";
    public static final  String API_USER_ANALYTICS_URL  = API_STRING + "/customer_analytics_info_by_id.php?phone=";
    public static final  String API_MOVIE_STATUS_URL    = API_STRING + "/minigrids_videos_status.php?video_id=";
    // DEMO stuff
    public static final  String API_DOMAIN_DEMO         = "http://seabasket.in/api/v1";
    public static final  String API_REGISTER_DEMO       = API_DOMAIN_DEMO + "/user/register.php?";
    public static final  String API_STATE_LIST_DEMO     = API_DOMAIN_DEMO + "/state.php?country_id=1&language=" + LANG_API;
    public static final  String API_COMPANY_LIST_DEMO   = API_DOMAIN_DEMO + "/company.php?language=" + LANG_API;
    public static final  String API_OTP_REQUEST_DEMO    = API_DOMAIN_DEMO + "/user/requestOTP.php?mobile=";
    public static final  String API_OTP_VERIFY_DEMO     = API_DOMAIN_DEMO + "/user/verifyOTP.php?";
    public static final  String API_VIDEO_UPLOAD        = API_DOMAIN_DEMO + "/upload.php?";
    public static final  String API_MOVIE_STATUS        = API_DOMAIN_DEMO + "/video.php?language=" + LANG_API + "&video_ids=";
    public static final  String API_FEATURED_VIDEO_DEMO = "http://www.minigridstories.com/api/v1/video?language=" + LANG_API + "&featured_only=1";
    public static final  String API_USER_VIDEO_DEMO     = "http://www.minigridstories.com/api/v1/video?language=" + LANG_API + "&username=Jitender%20Kumar%20Thakur";

    // TODO change language to user preferred
    public static final String PLATFORM_VIDEOS = API_DOMAIN + "/videos.php";
    public static final String VIEW_VIDEO_URL  = API_DOMAIN + "/view.php?video_id=";

    public static final int COUNTRY_INDIA = 1;

    public static final String PREF_FILE                 = "prefs";
    public static final char   SEPARATOR                 = '|';
    public static final String sp_lang                   = "lang";
    public static final String sp_first_time_open        = "first_time_open";
    public static final String sp_load_lang_select       = "load_lang_select";
    public static final String sp_load_intro_caro        = "load_intro_caro";
    public static final String sp_load_signup            = "load_signup";
    public static final String sp_user_name              = "user_name";
    public static final String sp_user_phone             = "user_phone";
    public static final String sp_user_company           = "user_company";
    public static final String sp_user_state             = "user_state";
    public static final String sp_user_district          = "user_district";
    public static final String sp_movie_prefix           = "m";
    public static final String sp_authorisation_key      = "auth_key";
    public static final String sp_amain_page_land_counts = "amain_page_land_counts";

    public static final String sp_company_list_json   = "company_list_json";
    public static final String sp_state_list_json     = "state_list_json";
    public static final String sp_user_analytics_json = "user_analytics_json";

    public static final String sp_featured_videos_response = "featured_video_response";
    public static final String sp_user_videos_response     = "user_video_response";

    // tutorial videos
    public static final String sp_show_dekho_tuto       = "show_dekho_tuto";
    public static final String sp_show_sunao_tuto       = "show_sunao_tuto";
    public static final String sp_show_sunao_audio_tuto = "show_sunao_audio_tuto";

    // overlays
    public static final String sp_show_help_overlay   = "show_help_overlays";
    public static final String sp_show_banao_overlays = "show_banao_overlays";
    public static final String sp_show_home_overlay   = "show_home_overviews";

    public static final String sp_show_dikhao_cam_intro_overlay = "show_dikhao_cam_intro_overlay";
    public static final String sp_show_batao_cam_intro_overlay  = "show_batao_cam_intro_overlay";
    public static final String sp_show_view_clip_overlay        = "show_view_clip_overlay";
    public static final String sp_show_overview_overlay         = "show_overview_overlay";
    public static final String sp_show_audio_intro_overlay      = "show_audio_intro_overlay";

    // congos
    public static final String sp_show_first_clip_congo      = "show_first_clip_congo";
    public static final String sp_show_first_scene_congo     = "show_first_scene_congo";
    public static final String sp_show_dikhao_complete_congo = "show_dikho_complete_congo";
    public static final String sp_all_finish_congo           = "show_all_finish_congo";

    public static final String WORKING_FOLDER = "MG5";
    // these nulls are initialized in ASplash's setupFileStructureAndFiles() method
    public static       String INTERNAL_ROOT  = null;
    public static       String EXTERNAL_ROOT  = null;

    public static       String PROJECTS_DIR        = C.INTERNAL_ROOT + "/projects";
    public static       String COMMON_DIR          = C.INTERNAL_ROOT + "/common";
    public static       String TEMP_DIR            = C.INTERNAL_ROOT + "/temp";
    public static       String MOVIES_EXAMPLES_DIR = C.INTERNAL_ROOT + "/movies_egs";
    public static       String MOVIES_DIR          = C.EXTERNAL_ROOT + "/movies"; // store movies in external folder
    public static final String MOVIE_EG_FILE_NAME  = "meg";

    public static final String DIKHAO_TUTORIAL_VIDEO_NAME  = "dikhao_tutorial";
    public static final String SELFIE_TUTORIAL_VIDEO_NAME  = "selfie_tutorial";
    public static final String AUDIO_TUTORIAL_VIDEO_NAME   = "audio_tutorial";
    public static final String EDIT_TUTORIAL_VIDEO_NAME    = "editing_tutorial";
    public static final String PUBLISH_TUTORIAL_VIDEO_NAME = "publish_tutorial";
    public static final String OVERVIEW_VIDEO_NAME         = "overview";
    public static final String MUXING_ANIMATION            = "muxing_animation";
    public static final String WAVE_ANIMATION              = "wave";

    public static final String[] tutorialVideoNames = {
            OVERVIEW_VIDEO_NAME,
            DIKHAO_TUTORIAL_VIDEO_NAME,
            SELFIE_TUTORIAL_VIDEO_NAME,
            AUDIO_TUTORIAL_VIDEO_NAME,
            EDIT_TUTORIAL_VIDEO_NAME,
            PUBLISH_TUTORIAL_VIDEO_NAME
    };

    public static final int MAX_USERNAME_LENGTH = 25;

    public static final boolean HD_RECORD_MODE  = true;
    public static final boolean TRY_WEBM        = false;
    public static final String  VIDEO_EXTENSION = TRY_WEBM ? "webm" : "mp4";
    public static final String  AUDIO_EXTENSION = TRY_WEBM ? "webm" : "aac";

}
