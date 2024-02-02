package com.sharifyy;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class OTPAuthenticatorFactory implements AuthenticatorFactory {

	private static final Logger LOG = Logger.getLogger(OTPAuthenticatorFactory.class);


	@Override
	public String getId() {
		return "javatalks-otp-authenticator";
	}

	@Override
	public String getDisplayType() {
		return "JavaTalks-OTP-Authentication";
	}

	@Override
	public String getHelpText() {
		return "sends OTP code via kafka";
	}

	@Override
	public String getReferenceCategory() {
		return "otp";
	}

	@Override
	public boolean isConfigurable() {
		return true;
	}

	@Override
	public boolean isUserSetupAllowed() {
		return false;
	}

	private KafkaEventProducer kafkaEventProducer;

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return new AuthenticationExecutionModel.Requirement[] {
			AuthenticationExecutionModel.Requirement.REQUIRED,
			AuthenticationExecutionModel.Requirement.ALTERNATIVE,
			AuthenticationExecutionModel.Requirement.DISABLED,
		};
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return Arrays.asList(
			new ProviderConfigProperty("length", "Code length", "The number of digits of the generated code.", ProviderConfigProperty.STRING_TYPE, 6),
			new ProviderConfigProperty("ttl", "Time-to-live", "The time to live in seconds for the code to be valid.", ProviderConfigProperty.STRING_TYPE, "300")
		);
	}

	@Override
	public Authenticator create(KeycloakSession session) {
		LOG.info("creating authenticator");
		return new OTPAuthenticator(kafkaEventProducer);
	}

	@Override
	public void init(Config.Scope config) {
		LOG.info("authenticator init");
		var bootstrapServers = config.get("bootstrapServers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
		Objects.requireNonNull(bootstrapServers, "bootstrapServers must not be null");
		var kafkaProperties = KafkaConfig.init(config);
		this.kafkaEventProducer = new KafkaEventProducer(bootstrapServers,kafkaProperties);
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
		LOG.info("authenticator post init");
	}

	@Override
	public void close() {
	}

}
