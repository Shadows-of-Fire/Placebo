function initializeCoreMod() {
    return {
        'placebo_get_ench_level_event': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeItemStack',
                'methodName': 'getAllEnchantments',
                'methodDesc': '()Ljava/util/Map;'
            },
            'transformer': function(method) {
                var owner = "dev/shadowsoffire/placebo/events/PlaceboEventFactory";
                var name = "getEnchantmentLevel";
                var desc = "(Ljava/util/Map;Lnet/minecraftforge/common/extensions/IForgeItemStack;)Ljava/util/Map;";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                ASMAPI.log('INFO', 'Patching IForgeItemStack#getEnchantmentLevel');

                var insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insn.add(ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC));
                instr.insertBefore(instr.getLast().getPrevious(), insn);

                return method;
            }
        }
    }
}