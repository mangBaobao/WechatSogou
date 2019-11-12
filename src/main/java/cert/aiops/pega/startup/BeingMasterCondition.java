package cert.aiops.pega.startup;

import cert.aiops.pega.bean.PegaEnum;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author He
 */
public class BeingMasterCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
      //  String  wantedRole = "online";
        String wantedRole = String.valueOf(PegaEnum.NodeRole.online);
        String configuredRole = conditionContext.getEnvironment().getProperty("pega.role");
        if (configuredRole.equals(wantedRole))
            return true;
        return false;
    }
}
