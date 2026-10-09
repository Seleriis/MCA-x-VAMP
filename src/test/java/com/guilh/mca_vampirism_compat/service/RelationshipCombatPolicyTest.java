package com.guilh.mca_vampirism_compat.service;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class RelationshipCombatPolicyTest {
    @Test void immediateFamilyIsSymmetricAndUnknownParentsAreNotShared() {
        UUID a=UUID.randomUUID(),b=UUID.randomUUID(),parent=UUID.randomUUID(),zero=new UUID(0,0);
        var first=new McaRelationshipCombatPolicy.FamilyLink(a,b,true,parent,zero,Set.of());
        var spouse=new McaRelationshipCombatPolicy.FamilyLink(b,a,true,zero,zero,Set.of());
        assertTrue(McaRelationshipCombatPolicy.related(first,spouse)); assertTrue(McaRelationshipCombatPolicy.related(spouse,first));
        var sibling=new McaRelationshipCombatPolicy.FamilyLink(UUID.randomUUID(),zero,false,parent,zero,Set.of());
        assertTrue(McaRelationshipCombatPolicy.related(first,sibling));
        var child=new McaRelationshipCombatPolicy.FamilyLink(UUID.randomUUID(),zero,false,a,zero,Set.of());
        assertTrue(McaRelationshipCombatPolicy.related(first,child));
        var unrelated=new McaRelationshipCombatPolicy.FamilyLink(UUID.randomUUID(),zero,false,zero,zero,Set.of());
        assertFalse(McaRelationshipCombatPolicy.related(spouse,unrelated));
        var divorced=new McaRelationshipCombatPolicy.FamilyLink(b,a,false,zero,zero,Set.of());
        assertFalse(McaRelationshipCombatPolicy.related(divorced,unrelated));
    }
}
