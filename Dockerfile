FROM openjdk:11-jdk
WORKDIR /apps

ENV APP_HOME=/apps/option-trading
ENV RUN_ENV=dev

RUN mkdir -p /apps/option-trading/dev
RUN mkdir -p /apps/option-trading/dev/lib
RUN mkdir -p /apps/option-trading/dev/bin/container
RUN mkdir -p /apps/option-trading/dev/config/env/default
RUN mkdir -p /apps/option-trading/dev/config/env/dev
RUN mkdir -p /apps/option-trading/dev/config/env/default/business-service/

RUN pwd
RUN ls -ltr

ADD /OptionTradeExecutorService/target/option-trade-executor-service-1.0.0-SNAPSHOT.jar option-trading/dev/
ADD /OptionTradeExecutorService/target/lib/* option-trading/dev/lib/
ADD /OptionTradeExecutorService/src/main/bin/container/* option-trading/dev/container/
ADD /OptionTradeExecutorService/src/main/config/env/default/default.properties option-trading/dev/config/env/default/default.properties
ADD /OptionTradeExecutorService/src/main/config/env/default/pod-service-mapping.properties option-trading/dev/config/env/default/pod-service-mapping.properties
ADD /OptionTradeExecutorService/src/main/config/env/default/business-service/* option-trading/dev/config/env/default/business-service/
ADD /OptionTradeExecutorService/src/main/config/env/dev/* option-trading/dev/config/env/dev/

RUN chmod -R 777 option-trading/dev/container/k8s-start-compute-service.sh
RUN chmod -R 777 option-trading/dev/container/k8s-start-main.sh

EXPOSE 20400
ENTRYPOINT ["/bin/bash"]
CMD ["option-trading/dev/container/k8s-start-compute-service.sh"]
