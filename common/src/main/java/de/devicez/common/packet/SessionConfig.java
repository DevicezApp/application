package de.devicez.common.packet;

import org.snf4j.core.EndingAction;
import org.snf4j.core.codec.DefaultCodecExecutor;
import org.snf4j.core.codec.ICodecExecutor;
import org.snf4j.core.session.DefaultSessionConfig;

public class SessionConfig extends DefaultSessionConfig {

    @Override
    public ICodecExecutor createCodecExecutor() {
        final DefaultCodecExecutor executor = new DefaultCodecExecutor();
        executor.getPipeline().add("DECODER", new PacketDecoder());
        executor.getPipeline().add("ENCODER", new PacketEncoder());
        return executor;
    }

    @Override
    public EndingAction getEndingAction() {
        return EndingAction.DEFAULT;
    }
}
