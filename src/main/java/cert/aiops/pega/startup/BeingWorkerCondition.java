package cert.aiops.pega.startup;

import cert.aiops.pega.util.PegaEnum;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public  class BeingWorkerCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String  wantedRole =String.valueOf(PegaEnum.NodeRole.worker);
        String configuredRole = conditionContext.getEnvironment().getProperty("pega.role");
        if (configuredRole.equals(wantedRole))
            return true;
        return false;
    }
}
