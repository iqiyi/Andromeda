// IDisplayTalkShow.aidl
package wang.imallen.blog.moduleexportlib.show;

// Declare any non-default types here with import statements

interface IDisplayTalkShow {
    String getShowName();

    String[]getHostNames();

    void startTalkShow(int userId);
}
