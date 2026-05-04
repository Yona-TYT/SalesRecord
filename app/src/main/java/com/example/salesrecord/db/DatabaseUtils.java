package com.example.salesrecord.db;

import com.example.salesrecord.db.dao.GenericDao;
import java.util.List;

public class DatabaseUtils {

    /**
     * Genera un ID único basado en un prefijo y el DAO genérico.
     * Busca el primer slot vacío en la secuencia (ej. "payID0", "payID1", etc.).
     * @param basePrefix Prefijo para el ID (ej. "payID")
     * @param dao El DAO de la entidad correspondiente
     * @return El nuevo ID generado
     */
    public static <T> String generateId(String basePrefix, GenericDao<T> dao) {
        List<T> allEntities = dao.getUsers();
        int mSiz = allEntities.size();
        String mIdx = basePrefix + "0";
        if (mSiz > 0) {
            mIdx = basePrefix + mSiz;
        }
        for (int i = 0; i < mSiz; i++) {
            T entity = dao.getUsers(basePrefix + i);
            if (entity == null) {
                mIdx = basePrefix + i;
                break;
            }
        }
        return mIdx;
    }

    /**
     * Ejemplo de otra utilidad: Cuenta el número total de entidades en un DAO.
     * Útil para validaciones o reportes.
     * @param dao El DAO genérico
     * @return Número de entidades
     */
    public static <T> int countEntities(GenericDao<T> dao) {
        return dao.getUsers().size();
    }

    /**
     * Ejemplo: Limpia todas las entidades de un DAO (usa con cuidado, en transacciones).
     * @param dao El DAO genérico
     */
    public static <T> void clearAll(GenericDao<T> dao) {
        // Asumiendo que agregas un método deleteAll() en GenericDao o en los DAOs
        // dao.deleteAll();  // Implementa si lo necesitas
    }

    // Puedes agregar más: validaciones de schema, backups, migraciones helpers, etc.
}
