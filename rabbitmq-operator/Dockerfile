FROM openjdk:11.0.1-jre
RUN groupadd -r -g 999 rabbitmq-operator
RUN useradd --no-log-init -r -u 999 -g rabbitmq-operator rabbitmq-operator
COPY build/distributions/rabbitmq-operator.zip /app/rabbitmq-operator.zip
RUN chown rabbitmq-operator:rabbitmq-operator -R /app
USER rabbitmq-operator
WORKDIR /app
RUN unzip rabbitmq-operator.zip
CMD ["java", "-cp", "rabbitmq-operator/lib/*", "-Dindeed.application=RabbitMQOperator", "-Dindeed.staging.level=local", "com.indeed.operators.rabbitmq.RabbitMQOperator"]
