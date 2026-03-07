package nsk.nu.ashcore.api.spi.testing;

public final class DuplicateServiceA implements DuplicateService {
    @Override
    public String id() {
        return "test:duplicate";
    }
}

