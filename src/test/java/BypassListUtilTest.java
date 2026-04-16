import com.themisterfish.invalidplayerbypasslist.util.BypassListUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BypassListUtilTest {

    private static final Path FILE_PATH = Path.of("invalidbypasslist.json");

    @AfterEach
    void resetCache() throws Exception {
        Files.deleteIfExists(FILE_PATH);
        overrideCache();
    }

    private static void overrideCache() throws Exception {
        Field cacheField = BypassListUtil.class.getDeclaredField("bypasslistCache");
        cacheField.setAccessible(true);
        cacheField.set(null, null);
    }

    @Test
    void testAddPlayer_NewEntry_ReturnsTrue() {
        boolean result = BypassListUtil.addPlayer("TheMisterFish", "1.2.3.4");
        assertTrue(result);

        List<String> players = BypassListUtil.getAllPlayers();
        assertEquals(List.of("themisterfish"), players);
    }

    @Test
    void testAddPlayer_Duplicate_ReturnsFalse() {
        BypassListUtil.addPlayer("TheMisterFish", "1.2.3.4");
        boolean result = BypassListUtil.addPlayer("TheMisterFish", "1.2.3.4");

        assertFalse(result);
        assertEquals(1, BypassListUtil.getIpsForPlayer("TheMisterFish").size());
    }

    @Test
    void testAddPlayer_NullIpStoredAsNone() {
        BypassListUtil.addPlayer("TestUser", null);

        List<String> ips = BypassListUtil.getIpsForPlayer("TestUser");
        assertEquals(List.of("none"), ips);
    }

    @Test
    void testRemovePlayer_RemovesAllIps() {
        BypassListUtil.addPlayer("TheMisterFish", "1.2.3.4");
        BypassListUtil.addPlayer("TheMisterFish", "5.6.7.8");

        boolean removed = BypassListUtil.removePlayer("TheMisterFish");

        assertTrue(removed);
        assertTrue(BypassListUtil.getAllPlayers().isEmpty());
    }

    @Test
    void testRemovePlayer_PlayerNotFound_ReturnsFalse() {
        boolean removed = BypassListUtil.removePlayer("Unknown");
        assertFalse(removed);
    }

    @Test
    void testRemovePlayerByIp_RemovesOnlyMatchingIp() {
        BypassListUtil.addPlayer("TheMisterFish", "1.2.3.4");
        BypassListUtil.addPlayer("TheMisterFish", "5.6.7.8");

        boolean removed = BypassListUtil.removePlayer("TheMisterFish", "1.2.3.4");

        assertTrue(removed);
        assertEquals(List.of("5.6.7.8"), BypassListUtil.getIpsForPlayer("TheMisterFish"));
    }

    @Test
    void testRemovePlayerByIp_NotFound_ReturnsFalse() {
        BypassListUtil.addPlayer("TheMisterFish", "1.2.3.4");

        boolean removed = BypassListUtil.removePlayer("TheMisterFish", "9.9.9.9");

        assertFalse(removed);
        assertEquals(List.of("1.2.3.4"), BypassListUtil.getIpsForPlayer("TheMisterFish"));
    }

    @Test
    void testGetAllPlayers_SortedAndUnique() {
        BypassListUtil.addPlayer("Charlie", "1");
        BypassListUtil.addPlayer("alice", "2");
        BypassListUtil.addPlayer("bob", "3");
        BypassListUtil.addPlayer("alice", "4");

        List<String> players = BypassListUtil.getAllPlayers();

        assertEquals(List.of("alice", "bob", "charlie"), players);
    }

    @Test
    void testGetIpsForPlayer_SortedAndUnique() {
        BypassListUtil.addPlayer("TheMisterFish", "5.6.7.8");
        BypassListUtil.addPlayer("TheMisterFish", "1.2.3.4");
        BypassListUtil.addPlayer("TheMisterFish", "1.2.3.4");

        List<String> ips = BypassListUtil.getIpsForPlayer("TheMisterFish");

        assertEquals(List.of("1.2.3.4", "5.6.7.8"), ips);
    }

    @Test
    void testPersistence_FileIsWrittenAndReloaded() throws Exception {
        BypassListUtil.addPlayer("TheMisterFish", "1.2.3.4");

        List<String> players = BypassListUtil.getAllPlayers();
        assertEquals(List.of("themisterfish"), players);
    }

    @Test
    void testLowercaseNormalization() {
        BypassListUtil.addPlayer("TheMISTerFIsh", "ABC.DEF");

        assertEquals(List.of("themisterfish"), BypassListUtil.getAllPlayers());
        assertEquals(List.of("abc.def"), BypassListUtil.getIpsForPlayer("TheMisterFish"));
    }

    @Test
    public void testIsInBypassList_HasNoneAlwaysTrue() {
        BypassListUtil.addPlayer("testNone", "none");

        assertTrue(BypassListUtil.isInBypassList("testNone", "1.2.3.4"));
        assertTrue(BypassListUtil.isInBypassList("testNone", "8.8.8.8"));
        assertTrue(BypassListUtil.isInBypassList("testNone", "none"));

        BypassListUtil.removePlayer("testNone");
    }

    @Test
    public void testIsInBypassList_NoNoneMatchIp() {
        BypassListUtil.addPlayer("testMatch", "1.2.3.4");

        assertTrue(BypassListUtil.isInBypassList("testMatch", "1.2.3.4"));

        BypassListUtil.removePlayer("testMatch");
    }

    @Test
    public void testIsInBypassList_NoNoneWrongIp() {
        BypassListUtil.addPlayer("testWrong", "1.2.3.4");

        assertFalse(BypassListUtil.isInBypassList("testWrong", "5.6.7.8"));
        assertFalse(BypassListUtil.isInBypassList("testWrong", "none"));

        BypassListUtil.removePlayer("testWrong");
    }
}
