package studentknowledge.service;

/**
 * 会话管理 - 存储登录Token和用户信息
 */
public class SessionManager {
    private static String token;
    private static Long userId;
    private static String username;
    private static String email;
    private static String role;

    public static void login(String t, Long uid, String uname, String em, String r) {
        token = t; userId = uid; username = uname; email = em; role = r;
    }
    public static void logout() {
        token = null; userId = null; username = null; email = null; role = null;
    }

    public static String  getToken()    { return token; }
    public static Long    getUserId()   { return userId; }
    public static String  getUsername()  { return username; }
    public static String  getEmail()    { return email; }
    public static String  getRole()     { return role; }
    public static boolean isAdmin()     { return "ADMIN".equals(role); }
    public static boolean isLoggedIn()  { return token != null; }
}
