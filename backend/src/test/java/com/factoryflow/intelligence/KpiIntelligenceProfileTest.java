package com.factoryflow.intelligence;
import static org.assertj.core.api.Assertions.*;
import com.factoryflow.intelligence.domain.KpiIntelligenceProfile;
import com.factoryflow.kpi.domain.KpiDefinition;
import java.util.List;
import org.junit.jupiter.api.Test;
class KpiIntelligenceProfileTest {
    private KpiIntelligenceProfile profile() { return KpiIntelligenceProfile.defaults(KpiDefinition.create("TEMP", "Température", null, "°C", null, null, true, List.of())); }
    @Test void defaultAndValidBusinessConfigurationAreExplicit() {
        var profile = profile(); assertThat(profile.isEnabled()).isTrue(); assertThat(profile.getForecastHorizon()).isEqualTo(7);
        assertThat(profile.getExpectedCadenceDays()).isNull(); assertThat(profile.getHistoryWindowDays()).isEqualTo(365);
        profile.update(true, 1, 14, 7, 730);
        assertThat(profile.getExpectedCadenceDays()).isEqualTo(1); assertThat(profile.getSeasonalPeriod()).isEqualTo(7);
    }
    @Test void rejectsInvalidCadenceHorizonSeasonalityAndWindow() {
        assertThatThrownBy(() -> profile().update(true, 0, 7, 7, 365)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> profile().update(true, 1, 31, 7, 365)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> profile().update(true, 1, 7, 1, 365)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> profile().update(true, 1, 7, 7, 29)).isInstanceOf(IllegalArgumentException.class);
    }
}
