package com.cdcrane.ekkochatsrv.config;

import org.springframework.modulith.ApplicationModule;

/**
 * This module should only depend on exceptions from other modules.
 */
@ApplicationModule(
        allowedDependencies = {
            "auth::exceptions",
            "users::exceptions"
        }
)
public class ConfigModule {
}
