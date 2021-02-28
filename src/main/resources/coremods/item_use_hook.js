function initializeCoreMod() {
    return {
        'placeboitemusehook': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.item.ItemStack',
                'methodName': 'func_196084_a',
                'methodDesc': '(Lnet/minecraft/item/ItemUseContext;)Lnet/minecraft/util/ActionResultType;'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                var instr = method.instructions;
                ASMAPI.log('INFO', 'Patching ItemStack#onItemUse');

                var insn = new InsnList();
                var label = new LabelNode();
                insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "shadows/placebo/events/PlaceboEventHooks", "onItemUse", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemUseContext;)Lnet/minecraft/util/ActionResultType;", false));
                insn.add(new InsnNode(Opcodes.DUP));
                insn.add(new JumpInsnNode(Opcodes.IFNULL, label));
                insn.add(new InsnNode(Opcodes.ARETURN));
                insn.add(label);
                insn.add(new InsnNode(Opcodes.POP));
                instr.insert(insn);

                return method;
            }
        }
    }
}