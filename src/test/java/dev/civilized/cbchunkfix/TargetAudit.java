package dev.civilized.cbchunkfix;

import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

/** Audits the actual installed target, without starting or modifying Minecraft. */
public final class TargetAudit {
    public static void main(String[] args) throws Exception {
        ClassNode cls = new ClassNode();
        try (ZipFile jar = new ZipFile(Path.of(args[0]).toFile())) {
            var entry = jar.getEntry("mod/chiselsandbits/client/logic/ChiseledBlockModelUpdateHandler.class");
            if (entry == null) throw new AssertionError("Missing target class");
            new ClassReader(jar.getInputStream(entry)).accept(cls, 0);
        }
        MethodNode method = cls.methods.stream().filter(m -> m.name.equals("updateAllModelDataInChunk")).findFirst().orElseThrow();
        if (!method.desc.equals("(Lnet/minecraft/world/level/chunk/LevelChunk;)V") || (method.access & Opcodes.ACC_STATIC)==0)
            throw new AssertionError("Unexpected target signature");
        int matches = 0;
        for (var instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call
                    && call.owner.equals("java/util/Collection") && call.name.equals("forEach")
                    && call.desc.equals("(Ljava/util/function/Consumer;)V")) {
                matches++;
                // Verify the redirect's stack contract, including the enclosing
                // method's chunk argument appended after Collection and Consumer.
                method.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
                method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "audit/RedirectStub", "refresh",
                        "(Ljava/util/Collection;Ljava/util/function/Consumer;Lnet/minecraft/world/level/chunk/LevelChunk;)V", false));
            }
        }
        if (matches != 1) throw new AssertionError("Expected exactly one injection site, got "+matches);
        method.maxStack++;
        new Analyzer<>(new BasicVerifier()).analyze(cls.name, method);
        System.out.println("PASS: installed Chisels & Bits target has exactly one matching call; static signature and redirect operand-stack contract verified. Target jar was not modified.");
    }
}
