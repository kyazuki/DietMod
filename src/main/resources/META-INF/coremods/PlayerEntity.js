var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var mappedMethodName = ASMAPI.mapMethod("func_213305_a");

function initializeCoreMod() {
    return {
        'coremodmethod': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.player.PlayerEntity',
                'methodName': mappedMethodName,
                'methodDesc': '(Lnet/minecraft/entity/Pose;)Lnet/minecraft/entity/EntitySize;'
            },
            'transformer': function(method) {
                var instruction = ASMAPI.findFirstInstruction(method, Opcodes.GETSTATIC);
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var newInstruction = new VarInsnNode(Opcodes.ALOAD, 0);
                method.instructions.set(instruction, newInstruction);
                var instruction2 = ASMAPI.findFirstInstruction(method, Opcodes.INVOKEINTERFACE);
                method.instructions.remove(instruction2.getPrevious());
                method.instructions.remove(instruction2.getNext());
                var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
                var newInstruction2 = new MethodInsnNode(Opcodes.INVOKESTATIC, 'com/github/kyazuki/dietmod/events/CommonEventHandler', 'getScaledPlayerSize', '(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Pose;)Lnet/minecraft/entity/EntitySize;');
                method.instructions.set(instruction2, newInstruction2);
                return method;
            }
        }
    }
}