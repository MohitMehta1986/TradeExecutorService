package executorservice.cache;

import java.util.Objects;

public class CacheKey {

    private final String symbol;
    private final String keyColumn;

    private CacheKey(String symbol, String keyColumn)
    {
        this.symbol = symbol;
        this.keyColumn = keyColumn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey key = (CacheKey) o;
        return this.symbol.equals(key.symbol) && this.keyColumn.equals(key.keyColumn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, keyColumn);
    }

    @Override
    public String toString() {
        return "CacheKey{" +
                "symbol='" + symbol + '\'' +
                ", keyColumn='" + keyColumn + '\'' +
                '}';
    }

    public static CacheKey generateCacheKey(String symbol, String keyColumn)
    {
        return new CacheKey(symbol, keyColumn);
    }
}
