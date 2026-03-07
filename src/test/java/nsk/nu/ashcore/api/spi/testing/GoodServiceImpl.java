package nsk.nu.ashcore.api.spi.testing;

public final class GoodServiceImpl implements GoodService {
    @Override
    public String id() {
        return "test:good";
    }
}

