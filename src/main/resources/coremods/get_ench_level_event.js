function initializeCoreMod() {
    return {
        'placebo_get_ench_level_event': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeItemStack',
                'methodName': 'getEnchantmentLevel',
                'methodDesc': '(Lnet/minecraft/world/item/enchantment/Enchantment;)I'
            },
            'transformer': function(method) {
                var owner = "shadows/placebo/events/PlaceboEventFactory";
                var name = "getEnchantmentLevel";
                var desc = "(ILnet/minecraftforge/common/extensions/IForgeItemStack;Lnet/minecraft/world/item/enchantment/Enchantment;)I";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
                ASMAPI.log('INFO', 'Patching IForgeItemStack#getEnchantmentLevel');

                var insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
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