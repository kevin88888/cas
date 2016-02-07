package org.jasig.cas.adaptors.radius.authentication;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.adaptors.radius.authentication.handler.support.RadiusAuthenticationHandler;
import org.jasig.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurer;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceMultifactorPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The authentication provider for yubikey.
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("radiusAuthenticationProvider")
public class RadiusMultifactorAuthenticationProvider implements MultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${cas.radius.rank:0}")
    private int rank;

    @Autowired
    @Qualifier("radiusAuthenticationHandler")
    private RadiusAuthenticationHandler radiusAuthenticationHandler;

    @Override
    public boolean verify(final RegisteredService service) throws AuthenticationException {
        if (radiusAuthenticationHandler.canPing()) {
            return true;
        }

        final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
        if (policy != null && policy.getFailureMode() == RegisteredServiceMultifactorPolicy.FailureModes.OPEN) {
            logger.warn("RADIUS servers could not be reached. Since the authentication provider is configured to fail-open, "
                    + "authentication will proceed without RADIUS for service {}", service.getServiceId());
            return false;
        }

        throw new AuthenticationException();
    }

    @Override
    public String getId() {
        return RadiusMultifactorWebflowConfigurer.MFA_RADIUS_EVENT_ID;
    }

    @Override
    public int getOrder() {
        return this.rank;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final RadiusMultifactorAuthenticationProvider rhs = (RadiusMultifactorAuthenticationProvider) obj;
        return new EqualsBuilder()
                .append(this.rank, rhs.rank)
                .append(this.getId(), rhs.getId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(rank)
                .append(getId())
                .toHashCode();
    }
}
