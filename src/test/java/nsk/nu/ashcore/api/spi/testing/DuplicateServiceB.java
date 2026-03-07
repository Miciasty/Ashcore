package nsk.nu.ashcore.api.spi.testing;

public final class DuplicateServiceB implements DuplicateService {
    @Override
    public String id() {
        return "test:duplicate";
    }
}

