/*
 * Copyright (C) 2020 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.thermal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;

import androidx.preference.PreferenceManager;

import com.android.thermal.utils.FileUtils;

public final class ThermalUtils {

    // Profile indices (used in spinner position / SharedPreferences storage)
    protected static final int STATE_DEFAULT = 0;
    protected static final int STATE_GAMING_HEAVY = 1;
    protected static final int STATE_GAMING_MEDIUM = 2;
    protected static final int STATE_GAMING_LIGHT = 3;
    protected static final int STATE_BENCHMARK = 4;
    protected static final int STATE_CAMERA = 5;
    protected static final int STATE_CAMERA_4K = 6;
    protected static final int STATE_STREAMING = 7;
    protected static final int STATE_VIDEO_CHAT = 8;
    protected static final int STATE_NAVIGATION = 9;
    protected static final int STATE_PHONE = 10;
    protected static final int STATE_DIALER = 11;
    protected static final int STATE_YUANSHEN = 12;
    protected static final int STATE_HIGHFPS = 13;
    protected static final int STATE_CHARGE = 14;

    protected static final int STATE_COUNT = 15;

    // sconfig values mapped from device's thermal_message/sconfig → odm/etc/thermal-*.conf
    private static final String SCONFIG_DEFAULT          = "0";   // thermal-normal
    private static final String SCONFIG_GAMING_HEAVY     = "19";  // thermal-mgame
    private static final String SCONFIG_GAMING_MEDIUM    = "18";  // thermal-tgame
    private static final String SCONFIG_GAMING_LIGHT     = "1";   // thermal-huanji
    private static final String SCONFIG_BENCHMARK        = "6";   // thermal-nolimits
    private static final String SCONFIG_CAMERA           = "15";  // thermal-camera
    private static final String SCONFIG_CAMERA_4K        = "16";  // thermal-4k
    private static final String SCONFIG_STREAMING        = "11";  // thermal-video
    private static final String SCONFIG_VIDEO_CHAT       = "14";  // thermal-videochat
    private static final String SCONFIG_NAVIGATION       = "10";  // thermal-navigation
    private static final String SCONFIG_PHONE            = "5";   // thermal-phone
    private static final String SCONFIG_DIALER           = "8";   // thermal-youtube
    private static final String SCONFIG_YUANSHEN         = "20";  // thermal-yuanshen
    private static final String SCONFIG_HIGHFPS          = "26";  // thermal-highfps
    private static final String SCONFIG_CHARGE           = "27";  // thermal-chg-only

    private static final String[] SCONFIG_VALUES = {
        SCONFIG_DEFAULT, SCONFIG_GAMING_HEAVY, SCONFIG_GAMING_MEDIUM, SCONFIG_GAMING_LIGHT,
        SCONFIG_BENCHMARK, SCONFIG_CAMERA, SCONFIG_CAMERA_4K,
        SCONFIG_STREAMING, SCONFIG_VIDEO_CHAT, SCONFIG_NAVIGATION, SCONFIG_PHONE,
        SCONFIG_DIALER, SCONFIG_YUANSHEN, SCONFIG_HIGHFPS, SCONFIG_CHARGE
    };

    private static final String THERMAL_CONTROL = "thermal_control";

    // Profile prefixes for SharedPreferences storage
    private static final String[] THERMAL_PREFIXES = {
        /* not used for default */ "",
        "thermal.gaming_heavy=",
        "thermal.gaming_medium=",
        "thermal.gaming_light=",
        "thermal.benchmark=",
        "thermal.camera=",
        "thermal.camera_4k=",
        "thermal.streaming=",
        "thermal.video_chat=",
        "thermal.navigation=",
        "thermal.phone=",
        "thermal.dialer=",
        "thermal.yuanshen=",
        "thermal.highfps=",
        "thermal.charge="
    };

    private static final String THERMAL_SCONFIG = "/sys/devices/virtual/thermal/thermal_message/sconfig";

    // ── Known game package lists for automatic thermal profile assignment ──

    // Yuanshen — dedicated Genshin Impact thermal profile (thermal-yuanshen.conf)
    private static final String[] GAMES_YUANSHEN = {
        "com.miHoYo.GenshinImpact",             // Genshin Impact (Global)
        "com.miHoYo.Yuanshen",                   // Genshin Impact (CN)
    };

    // Heavy (AAA / GPU-intensive) → STATE_GAMING_HEAVY  (mgame)
    private static final String[] GAMES_HEAVY = {
        "com.miHoYo.hkrpg",                      // Honkai: Star Rail (Global)
        "com.miHoYo.hkrpgoversea",               // Honkai: Star Rail (CN)
        "com.HoYoverse.Nap",                     // Zenless Zone Zero
        "com.HoYoverse.Nap.Bilibili",            // Zenless Zone Zero (CN)
        "com.tencent.tmgp.kr",                   // Arena Breakout
        "com.dts.freefiremax",                   // Free Fire MAX
        "com.activision.callofduty.warzone",     // CoD: Warzone Mobile
        "com.netease.mrzhna",                    // Marvel Rivals
        "com.tencent.ig",                        // PUBG Mobile (CN)
        "com.pubg.krmobile",                     // PUBG Mobile (KR)
        "com.rekoo.pubgm",                       // PUBG Mobile (VN)
        "com.netease.dbdena",                    // Dead by Daylight Mobile
        "com.garena.game.codm",                  // CoD Mobile (Garena)
        "com.riotgames.league.wildrift",         // League of Legends: Wild Rift
        "com.riotgames.league.wildrifttw",       // Wild Rift (TW)
        "com.riotgames.league.wildriftvn",       // Wild Rift (VN)
        "com.netmarble.mherosgb",               // Marvel Future Revolution
        "com.pearlabyss.blackdesertm.gl",        // Black Desert Mobile
        "com.epicgames.fortnite",                // Fortnite
        "com.tencent.tmgp.pubgmhd",             // Game for Peace (CN PUBG)
        "com.lilithgame.hgame.gp",              // Dislyte
        "com.netease.g93na",                     // Naraka: Bladepoint Mobile
        "com.bluepoch.r1sea",                    // Reverse: 1999
        "com.levelinfinite.hotta.gp",           // Tower of Fantasy
        "com.proximabeta.mf.uamo",              // Delta Force Mobile
    };

    // Medium (competitive online / mid-range 3D) → STATE_GAMING_MEDIUM  (tgame)
    private static final String[] GAMES_MEDIUM = {
        "com.tencent.iglite",                    // PUBG Mobile Lite
        "com.pubg.newstate",                     // PUBG: New State
        "com.vng.pubgmobile",                    // PUBG Mobile (VNG)
        "com.tencent.tmgp.sgame",               // Honor of Kings (CN)
        "com.levelinfinite.sgameGlobal",         // Honor of Kings (Global)
        "com.activision.callofduty.shooter",     // CoD: Mobile
        "com.garena.game.fctw",                  // Free Fire (TW)
        "com.dts.freefireth",                    // Free Fire
        "com.supercell.clashofclans",            // Clash of Clans
        "com.supercell.clashroyale",             // Clash Royale
        "com.supercell.brawlstars",              // Brawl Stars
        "com.supercell.squad",                   // Squad Busters
        "com.mobile.legends",                    // Mobile Legends
        "com.mobilelegends.hwag",                // Mobile Legends (HW)
        "com.tencent.lolm",                      // LoL: Wild Rift (CN)
        "com.riot.league.wildrift",              // Wild Rift (alt)
        "com.ea.gp.fifamobile",                  // EA FC Mobile
        "com.ea.gp.apexlegendsmobilefps",        // Apex Legends Mobile
        "com.gameloft.android.ANMP.GlsoftAsphalt9", // Asphalt 9
        "com.miniclip.eightballpool",            // 8 Ball Pool
        "com.innersloth.spacemafia",             // Among Us
        "jp.konami.pesam",                       // eFootball
        "com.tencent.tmgp.cf",                   // CrossFire (CN)
        "com.gamedevltd.destinychi",             // Destiny 2 companion
        "com.netease.g78na.gb",                  // Diablo Immortal
        "com.plarium.raidlegends",               // Raid: Shadow Legends
        "com.miHoYo.bh3global",                  // Honkai Impact 3rd
        "com.miHoYo.bh3oversea",                 // Honkai Impact 3rd (SEA)
        "jp.co.craftegg.band",                   // BanG Dream
        "com.YoStarEN.Arknights",                // Arknights (Global)
        "com.hypergryph.arknights",              // Arknights (CN)
        "com.kakaogames.umamusume",              // Uma Musume
        "com.tencent.tmgp.speedmobile",          // QQ Speed
        "com.sega.ColorfulStage.en",             // Project SEKAI
        "com.mojang.minecraftpe",                // Minecraft
    };

    // Light (casual / 2D / puzzle / idle) → STATE_GAMING_LIGHT  (huanji)
    private static final String[] GAMES_LIGHT = {
        "com.kiloo.subwaysurf",                  // Subway Surfers
        "com.imangi.templerun2",                 // Temple Run 2
        "com.king.candycrushsaga",               // Candy Crush
        "com.king.candycrushsodasaga",           // Candy Crush Soda
        "com.halfbrick.fruitninjafree",          // Fruit Ninja
        "com.rovio.baba",                        // Angry Birds 2
        "com.outfit7.talkingtom",                // Talking Tom
        "com.nekki.shadowfight3",                // Shadow Fight 3
        "com.etermax.preguntados.lite",          // Trivia Crack
        "io.supercent.knifemaster",              // Knife Hit
        "com.zeptolab.ctr2.f2p.google",          // Cut the Rope 2
        "com.playrix.gardenscapes",              // Gardenscapes
        "com.playrix.homescapes",                // Homescapes
        "com.ea.game.pvz2_na",                   // Plants vs Zombies 2
        "com.robtopx.geometrydash",              // Geometry Dash
        "com.yodo1.crossyroad",                  // Crossy Road
        "com.dxx.firenow",                       // Stumble Guys
        "com.scopely.monopolygo",                // Monopoly GO
        "com.gram.worldwar",                     // World War Heroes
        "com.fungames.sniper3d",                 // Sniper 3D
        "com.roblox.client",                     // Roblox
        "com.sandboxol.blockymods",              // Sandbox
    };

    private Context mContext;
    private SharedPreferences mSharedPrefs;

    protected ThermalUtils(Context context) {
        mContext = context;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void startService(Context context) {
        if (FileUtils.fileExists(THERMAL_SCONFIG)) {
            context.startServiceAsUser(new Intent(context, ThermalService.class),
                    UserHandle.CURRENT);
        }
    }

    private void writeValue(String profiles) {
        mSharedPrefs.edit().putString(THERMAL_CONTROL, profiles).apply();
    }

    private String getValue() {
        String value = mSharedPrefs.getString(THERMAL_CONTROL, null);

        if (value != null) {
            String[] modes = value.split(":");
            if (modes.length < STATE_COUNT - 1) value = null;
        }

        if (value == null || value.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < THERMAL_PREFIXES.length; i++) {
                if (i > 1) sb.append(":");
                sb.append(THERMAL_PREFIXES[i]);
            }
            value = sb.toString();
            writeValue(value);
        }
        return value;
    }

    protected void writePackage(String packageName, int mode) {
        String value = getValue();
        value = value.replace(packageName + ",", "");
        String[] modes = value.split(":");

        if (mode >= 1 && mode < STATE_COUNT) {
            modes[mode - 1] = modes[mode - 1] + packageName + ",";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modes.length; i++) {
            if (i > 0) sb.append(":");
            sb.append(modes[i]);
        }

        writeValue(sb.toString());
    }

    protected int getStateForPackage(String packageName) {
        String value = getValue();
        String[] modes = value.split(":");

        for (int i = 0; i < modes.length; i++) {
            if (modes[i].contains(packageName + ",")) {
                return i + 1;
            }
        }
        return STATE_DEFAULT;
    }

    /**
     * Returns the effective state for a package, including auto-detection fallback.
     * Used by the UI to show the actual profile that will be applied.
     */
    protected int getEffectiveStateForPackage(String packageName) {
        int state = getStateForPackage(packageName);
        if (state == STATE_DEFAULT) {
            state = autoDetectProfile(packageName);
        }
        return state;
    }

    /**
     * Returns true if the package has a user-assigned profile (not auto-detected).
     */
    protected boolean isUserAssigned(String packageName) {
        return getStateForPackage(packageName) != STATE_DEFAULT;
    }

    protected void setDefaultThermalProfile() {
        FileUtils.writeLine(THERMAL_SCONFIG, SCONFIG_DEFAULT);
    }

    protected void setChargingThermalProfile() {
        FileUtils.writeLine(THERMAL_SCONFIG, SCONFIG_CHARGE);
    }

    /**
     * If the given package is currently in the foreground, apply its thermal profile immediately.
     * Called after the user manually assigns a profile in the UI.
     */
    protected void applyIfForeground(String packageName) {
        try {
            android.app.ActivityManager am = (android.app.ActivityManager)
                    mContext.getSystemService(android.content.Context.ACTIVITY_SERVICE);
            if (am == null) return;
            java.util.List<android.app.ActivityManager.RunningTaskInfo> tasks =
                    am.getRunningTasks(1);
            if (tasks != null && !tasks.isEmpty()) {
                android.content.ComponentName top = tasks.get(0).topActivity;
                if (top != null && packageName.equals(top.getPackageName())) {
                    setThermalProfile(packageName);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    protected void setThermalProfile(String packageName) {
        int state = getStateForPackage(packageName);
        if (state == STATE_DEFAULT) {
            int detected = autoDetectProfile(packageName);
            if (detected != STATE_DEFAULT) {
                writePackage(packageName, detected);
                state = detected;
            }
        }
        String sconfig = SCONFIG_VALUES[state];
        FileUtils.writeLine(THERMAL_SCONFIG, sconfig);
    }

    /**
     * Detect game profile based on known game packages.
     * Returns appropriate gaming state or STATE_DEFAULT if not a known game.
     */
    private int detectGameProfile(String packageName) {
        // Genshin Impact — has dedicated thermal-yuanshen.conf
        for (String pkg : GAMES_YUANSHEN) {
            if (packageName.equals(pkg)) return STATE_YUANSHEN;
        }
        // Heavy games — AAA / demanding titles that max out GPU/CPU
        for (String pkg : GAMES_HEAVY) {
            if (packageName.equals(pkg)) return STATE_GAMING_HEAVY;
        }
        // Medium games — competitive / popular online games
        for (String pkg : GAMES_MEDIUM) {
            if (packageName.equals(pkg)) return STATE_GAMING_MEDIUM;
        }
        // Light games — casual / 2D / puzzle games
        for (String pkg : GAMES_LIGHT) {
            if (packageName.equals(pkg)) return STATE_GAMING_LIGHT;
        }
        // Heuristic: package name contains known game-engine or gaming keywords
        String lower = packageName.toLowerCase();
        if (lower.contains("unreal") || lower.contains("unity")
                || lower.contains(".game.") || lower.contains(".games.")) {
            return STATE_GAMING_MEDIUM;
        }
        return STATE_DEFAULT;
    }

    /**
     * Auto-detect thermal profile based on app category, known packages and permissions.
     * This is used as fallback when the user hasn't manually assigned a profile.
     */
    private int autoDetectProfile(String packageName) {
        try {
            PackageManager pm = mContext.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);

            // 1. Check known game packages first (most accurate classification)
            int gameProfile = detectGameProfile(packageName);
            if (gameProfile != STATE_DEFAULT) {
                return gameProfile;
            }

            // 2. Check ApplicationInfo.category (API 26+)
            if (ai.category == ApplicationInfo.CATEGORY_GAME) {
                return STATE_GAMING_MEDIUM;
            }

            // 3. Check known package prefixes / names
            String pkg = packageName.toLowerCase();

            // Camera apps
            if (pkg.contains("camera") || pkg.contains("gcam")
                    || pkg.equals("com.android.camera")) {
                return STATE_CAMERA;
            }

            // Navigation / maps
            if (pkg.contains("maps") || pkg.contains("navigation") || pkg.contains("waze")
                    || pkg.equals("com.google.android.apps.maps")
                    || pkg.equals("com.autonavi.minimap")
                    || pkg.equals("ru.yandex.yandexnavi")
                    || pkg.equals("com.sygic.aura")) {
                return STATE_NAVIGATION;
            }

            // Video chat / conferencing
            if (pkg.contains("zoom") || pkg.contains("meet") || pkg.contains("teams")
                    || pkg.contains("duo") || pkg.contains("skype")
                    || pkg.contains("whatsapp") || pkg.contains("telegram")
                    || pkg.contains("facetime") || pkg.contains("discord")
                    || pkg.equals("com.google.android.apps.tachyon")
                    || pkg.equals("us.zoom.videomeetings")
                    || pkg.equals("com.microsoft.teams")) {
                return STATE_VIDEO_CHAT;
            }

            // Video streaming / media players
            if (pkg.contains("youtube") || pkg.contains("netflix") || pkg.contains("plex")
                    || pkg.contains("twitch") || pkg.contains("primevideo")
                    || pkg.contains("hulu") || pkg.contains("disney")
                    || pkg.contains("vlc") || pkg.contains("mxplayer")
                    || pkg.contains("video") || pkg.contains("player")
                    || pkg.contains("bilibili") || pkg.contains("iqiyi")
                    || pkg.equals("com.google.android.apps.youtube.music")) {
                return STATE_STREAMING;
            }

            // Dialer / phone
            if (pkg.contains("dialer") || pkg.contains("phone") || pkg.contains("incallui")
                    || pkg.equals("com.google.android.dialer")
                    || pkg.equals("com.android.phone")) {
                return STATE_PHONE;
            }

            // Benchmark apps
            if (pkg.contains("antutu") || pkg.contains("geekbench")
                    || pkg.contains("3dmark") || pkg.contains("benchmark")
                    || pkg.equals("com.futuremark.dmandroid.application")
                    || pkg.equals("com.primatelabs.geekbench6")
                    || pkg.equals("com.ivarna.finalbenchmark2")
                    || pkg.equals("com.franko.kernel")) {
                return STATE_BENCHMARK;
            }

            // 3. Check flags — old isGame flag for legacy apps
            if ((ai.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                return STATE_GAMING_MEDIUM;
            }

        } catch (PackageManager.NameNotFoundException e) {
            // Package not found, fall through to default
        }

        return STATE_DEFAULT;
    }
}
