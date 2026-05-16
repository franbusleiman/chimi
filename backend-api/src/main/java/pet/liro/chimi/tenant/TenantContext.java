package pet.liro.chimi.tenant;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(Long tenantId) {
        CURRENT.set(tenantId);
    }

    public static Long get() {
        return CURRENT.get();
    }

    public static Long require() {
        Long id = CURRENT.get();
        if (id == null) {
            throw new IllegalStateException("Tenant no resuelto en el contexto de la request");
        }
        return id;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
