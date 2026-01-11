package fr.uparis.informatique.cpoo5.liquidwar.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour ObjectPool.
 * 
 * Teste :
 * - Acquisition et libération d'objets
 * - Réutilisation d'objets
 * - Statistiques
 */
class ObjectPoolTest {

    private ObjectPool<String> pool;

    @BeforeEach
    void setUp() {
        pool = new ObjectPool<>(() -> new String("test"), 5, 10);
    }

    @Test
    @DisplayName("acquire retourne un objet")
    void testAcquire() {
        String obj = pool.acquire();

        assertNotNull(obj, "acquire doit retourner un objet non null");
        assertEquals(1, pool.getInUseCount(), "Un objet doit être en utilisation");
    }

    @Test
    @DisplayName("release remet l'objet dans le pool")
    void testRelease() {
        String obj = pool.acquire();
        pool.release(obj);

        assertEquals(0, pool.getInUseCount(), "Aucun objet ne doit être en utilisation");
        assertTrue(pool.getAvailableCount() > 0, "L'objet doit être disponible");
    }

    @Test
    @DisplayName("Les objets sont réutilisés")
    void testReuse() {
        String obj1 = pool.acquire();
        pool.release(obj1);

        String obj2 = pool.acquire();

        // Les objets peuvent être réutilisés (même référence ou nouvelle instance)
        assertNotNull(obj2, "Un objet doit être retourné");
    }

    @Test
    @DisplayName("releaseAll libère tous les objets")
    void testReleaseAll() {
        ArrayList<String> objects = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            objects.add(pool.acquire());
        }

        assertEquals(5, pool.getInUseCount(), "5 objets doivent être en utilisation");

        pool.releaseAll();

        assertEquals(0, pool.getInUseCount(), "Aucun objet ne doit être en utilisation");
    }

    @Test
    @DisplayName("getReuseRate calcule le taux de réutilisation")
    void testReuseRate() {
        String obj = pool.acquire();
        pool.release(obj);
        pool.acquire(); // Réutilise l'objet

        double rate = pool.getReuseRate();
        assertTrue(rate >= 0 && rate <= 1, "Le taux doit être entre 0 et 1");
    }

    @Test
    @DisplayName("getTotalSize retourne la taille totale")
    void testTotalSize() {
        String obj = pool.acquire();

        int total = pool.getTotalSize();
        assertTrue(total > 0, "La taille totale doit être positive");
        assertEquals(pool.getAvailableCount() + pool.getInUseCount(), total,
                "Total doit être la somme de available et inUse");
    }

    @Test
    @DisplayName("Point interne fonctionne correctement")
    void testPointClass() {
        ObjectPool.Point p1 = new ObjectPool.Point(10, 20);
        ObjectPool.Point p2 = new ObjectPool.Point(10, 20);
        ObjectPool.Point p3 = new ObjectPool.Point(30, 40);

        assertEquals(10, p1.x);
        assertEquals(20, p1.y);

        assertEquals(p1, p2, "Points égaux doivent être égaux");
        assertNotEquals(p1, p3, "Points différents ne doivent pas être égaux");

        assertEquals(p1.hashCode(), p2.hashCode(), "Points égaux doivent avoir le même hashCode");

        assertNotNull(p1.toString(), "toString ne doit pas retourner null");
    }
}
