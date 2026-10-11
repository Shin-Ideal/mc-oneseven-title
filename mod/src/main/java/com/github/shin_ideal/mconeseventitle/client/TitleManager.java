package com.github.shin_ideal.mconeseventitle.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;

public class TitleManager {

    private static String titleText = "";
    private static String subtitleText = "";
    private static String actionbarText = "";

    private static int fadeInTicks = 20;
    private static int stayTicks = 60;
    private static int fadeOutTicks = 20;

    private static int titleDisplayTicks = 0;
    private static int titleTotalTicks = 0;

    private static int actionbarDisplayTicks = 0;
    private static final int ACTIONBAR_STAY_TICKS = 60;
    private static final int ACTIONBAR_FADEOUT_TICKS = 20;

    public static synchronized void setTitle(String rawText) {
        titleText = parseText(rawText);
        titleTotalTicks = fadeInTicks + stayTicks + fadeOutTicks;
        titleDisplayTicks = titleTotalTicks;
    }

    public static synchronized void setSubtitle(String rawText) {
        subtitleText = parseText(rawText);
    }

    public static synchronized void setActionbar(String rawText) {
        actionbarText = parseText(rawText);
        actionbarDisplayTicks = ACTIONBAR_STAY_TICKS;
    }

    public static synchronized void setTimes(int fadeIn, int stay, int fadeOut) {
        if (fadeIn >= 0) {
            fadeInTicks = fadeIn;
        }
        if (stay >= 0) {
            stayTicks = stay;
        }
        if (fadeOut >= 0) {
            fadeOutTicks = fadeOut;
        }
    }

    public static synchronized void clear() {
        titleDisplayTicks = 0;
        actionbarDisplayTicks = 0;
        titleText = "";
        subtitleText = "";
        actionbarText = "";
    }

    public static synchronized void reset() {
        clear();
        fadeInTicks = 20;
        stayTicks = 60;
        fadeOutTicks = 20;
    }

    public static synchronized void clientTick() {
        if (titleDisplayTicks > 0) {
            titleDisplayTicks--;
            if (titleDisplayTicks == 0) {
                titleText = "";
                subtitleText = "";
            }
        }
        if (actionbarDisplayTicks > 0) {
            actionbarDisplayTicks--;
            if (actionbarDisplayTicks == 0) {
                actionbarText = "";
            }
        }
    }

    public static synchronized String getTitleText() {
        return titleText;
    }

    public static synchronized String getSubtitleText() {
        return subtitleText;
    }

    public static synchronized String getActionbarText() {
        return actionbarText;
    }

    public static synchronized float getTitleAlpha(float partialTicks) {
        if (titleDisplayTicks <= 0 || titleTotalTicks <= 0) {
            return 0.0F;
        }

        float age = (float) (titleTotalTicks - titleDisplayTicks) + partialTicks;

        if (fadeInTicks > 0 && age < (float) fadeInTicks) {
            return MathHelper.clamp_float(age / (float) fadeInTicks, 0.0F, 1.0F);
        } else if (age < (float) (fadeInTicks + stayTicks)) {
            return 1.0F;
        } else if (fadeOutTicks > 0 && age < (float) titleTotalTicks) {
            float remaining = (float) titleTotalTicks - age;
            return MathHelper.clamp_float(remaining / (float) fadeOutTicks, 0.0F, 1.0F);
        }

        return 0.0F;
    }

    public static synchronized float getActionbarAlpha(float partialTicks) {
        if (actionbarDisplayTicks <= 0) {
            return 0.0F;
        }

        if (actionbarDisplayTicks <= ACTIONBAR_FADEOUT_TICKS) {
            float remaining = (float) actionbarDisplayTicks - partialTicks;
            return MathHelper.clamp_float(remaining / (float) ACTIONBAR_FADEOUT_TICKS, 0.0F, 1.0F);
        }

        return 1.0F;
    }

    private static String parseText(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        // Clean up any leaked toString dumps like ChatComponentText{text='...', ...}
        if (raw.contains("ChatComponentText{") || raw.contains("style=Style{")) {
            Matcher m = Pattern.compile("text='([^']*)'")
                .matcher(raw);
            if (m.find()) {
                raw = m.group(1);
            }
        }
        try {
            IChatComponent component = IChatComponent.Serializer.func_150699_a(raw);
            if (component != null) {
                String text = component.getFormattedText();
                if (text.startsWith("\"") && text.endsWith("\"") && text.length() >= 2) {
                    text = text.substring(1, text.length() - 1);
                }
                return text;
            }
        } catch (Exception ignored) {
            // Not a JSON component or invalid format; fall back to plain text
        }
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
            raw = raw.substring(1, raw.length() - 1);
        }
        return raw.replace('&', '\u00a7');
    }
}
