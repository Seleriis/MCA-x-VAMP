## 1.3.1 — atualização de compatibilidade (2026-10-27)

- Pré-lançamento no GitHub: v1.3.1-1.20.1.
- Validação adicional em 28/10/2026: inicialização com JAR de produção, comandos básicos, salvamento e encerramento normal aprovados; detalhes em VALIDATION.md.
- Atualiza as dependências de compilação e validação para MCA Reborn 7.7.1-beta.5, Vampirism 1.10.17 e MCA × EV 1.1.9.
- Acrescenta verificação dos contratos de captura, restauração, colocação e reprodução do MCA × EV, incluindo os pontos usados para preservar os estados de facção.
- Mantém as otimizações locais existentes e os formatos salvos.
- Permite escolher uma saída de compilação nova com compat_build_dir, evitando artefatos antigos bloqueados pelo OneDrive.
- Resultado dos testes e limitações registrados em VALIDATION.md; pacote em releases/1.3.1/.

## 1.3.0 — candidato local para testes (2026-09-28; não publicado)

- Atualiza o alvo Forge para MCA Reborn 7.7.1-beta.3 e Vampirism 1.10.17.
- Migra imports e alvos de mixin para o pacote `forge.net.conczin.mca` e adapta registros de traits, comércio, equipamento e ataque às APIs atuais da MCA.
- Mantém a versão antiga 1.2.3 destinada à MCA 7.6.x; faça backup do mundo antes de trocar para MCA 7.7.1.
- Build Forge concluído; 92 testes unitários (10 ignorados) e 37 GameTests obrigatórios aprovados no perfil base MCA/Vampirism. Integrações opcionais não foram testadas em runtime nesta rodada.
- JAR, fontes, arquivo do projeto, notas e checksums locais em `releases/1.3.0/`. Não publicado.

## 1.2.3 — 2026-09-16

- Guia ampliado de 31 para 42 páginas, com instruções atuais sobre favores, recompensas, diário, prazos, profissões, família e Easy Villagers.
- Falas próprias para cada tipo de favor e texto mais natural ao aceitar, consultar e concluir pedidos; rótulos do diário simplificados.
- Tempo restante nas conversas passa a mostrar minutos quando falta menos de um dia, com singular correto. Os prazos salvos permanecem iguais.
- Revisão em português brasileiro, português europeu e inglês, preservando o livro e seus dois marcadores.

## 1.2.2 — 2026-09-15

- Dois marcadores laterais fixos: guia em cima, missões embaixo, com símbolos distintos e destaque da seção atual.
- Numeração das missões no topo, com a mesma posição e tradução do guia; a página vazia indica 1 de 1.
- Removido o link Guia do rodapé e restauradas as posições originais das setas.
- Orientação do diário vazio esclarece que pedidos exigem um adulto da facção numa vila controlada por totem; um vampiro numa vila comum não atende esse requisito. As regras das missões permanecem iguais.

## 1.2.1 — 2026-09-14

- Marcador lateral horizontal menor, com pergaminho e cores do Livro das Artes Ocultas.
- Missões apresentadas em páginas do livro original, preservando moldura, papel e setas.
- Uma missão por página, acompanhamento, retorno à página anterior do guia e rolagem para campos longos sem sobrepor os controles.
- Textos compactos e nomes traduzidos das dimensões padrão em pt_BR, pt_PT e en_US.
- Dados, prazos, recompensas e validação permanecem no servidor, com atualização do diário a cada segundo.

# Changelog

## 1.2.0 — 2026-09-12

- Seção de missões no Livro de Artes Ocultas, com paginação, acompanhamento, informações de localização, recompensa e prazo. Os pedidos pertencem ao jogador no servidor.
- Suprimentos após ataques e pedidos de especialistas para vampiros, caçadores e lobisomens. Contexto de ataque dura 24.000 ticks; ajuda urgente tem prioridade.
- Recompensas configuráveis por tipo, gravadas com o pedido. Pedidos antigos conservam cinco corações, quantidades e prazos.
- Formato de missões versionado, índice de pedidos ativos e limpeza periódica limitada de registros encerrados cujo cooldown terminou, incluindo jogadores desconectados.
- Moradores descarregados conservam seus pedidos. Morte confirmada, totem removido e controle incompatível encerram pedidos sem uma entrega de itens.
- Proteção opcional de família e amizade no combate, desativada por padrão; agressão confirmada permite retaliação com os limites existentes. Falas contextuais não alteram regras de comércio.
- Integração opcional de facção e cura nos novos tooltips do EV, mantendo suporte às versões legadas testadas.
- Novos pacotes de diário validados no servidor e protocolo 4.

## 1.1.19 - MCA x Easy Villagers Breeder fertility safety

- Adds a read-only server-side guard for MCA x Easy Villagers 1.0.2: an MCA
  parent carrying MCA's native `INFERTILE` trait cannot create a child in the
  Breeder. The cancelled attempt keeps both parents, food and output intact.
- Breeding continues through MCA's native child factory for fertile parents,
  preserving MCA genetics, traits, family and relationship state in the child
  snapshot.
- No save, ForgeCaps, trait ID, payload or TOML value changed.

## 1.1.18 - Optional Werewolves safety

- Do not register or expose the MCA Werewolf trait when the optional
  Werewolves addon is absent. Existing Werewolf state stays inert in saved
  villagers and becomes active again if the addon is installed later.
- Audit MCA x Easy Villagers' Converter transaction: a golden apple and
  weakness potion are held while conversion can still fail or has no output
  space, then consumed exactly once when a valid converted result is committed.
  This avoids ingredient loss during rollback.
- The 1.20.1 MCA x Easy Villagers integration deliberately has no Inventory
  Viewer surface; its stored Hunter equipment therefore has no viewer-copy
  path to patch on this line.
- Saves, trait IDs, ForgeCaps, payloads and TOML values are unchanged.

## 1.1.17 - MCA explicit skin-dye hotfix

- MCA 7.6.28-beta.10's editor can apply an explicit skin dye, which takes
  precedence over its melanin and hemoglobin renderer. A Vampire villager now
  temporarily clears that dye so the configured pale genetic skin appears.
- The exact original dye is captured and restored when the Vampire trait is
  removed or the villager is cured. Version-two appearance snapshots capture
  their still-live dye once before applying the fix, so existing worlds keep
  their customized skin colour.
- MCA 7.6.16 has no skin-dye API; the reflective bridge is a safe no-op there.
  Save compatibility, trait IDs, ForgeCaps/payload keys and TOML keys are
  unchanged; the existing appearance snapshot now records its real skin-dye
  value when MCA exposes one.

## 1.1.16 - MCA dialogue trade hotfix

- Fixes MCA 7.6.28-beta.10's dialogue `trade` command, which calls its
  vanilla `beginTradeWith` invoker directly instead of `copiedBeginTradeWith`.
  Rival factions are now refused before that invoker can open a merchant menu.
- The command route emits `TRADE_GATE route=DIALOGUE_COMMAND`, with the real
  and effective faction plus the final decision, alongside the existing
  empty-hand interaction diagnostic.
- Saves, traits, IDs, NBT/ForgeCaps, payloads and TOML keys are unchanged.

## 1.1.15 - Hunter trade-gate hotfix

- Fixes the MCA 7.6.28 direct empty-hand interaction route that could open a
  Hunter merchant for a Vampire before MCA reached its copied merchant helper.
  The Forge interaction event and MCA's interaction-method head now enforce
  the same faction decision as the existing merchant-helper hook.
- Adds one concise `TRADE_GATE` INFO record per direct special-villager
  interaction. It records the route, real/effective player faction, detected
  villager faction and decision, making a deliberate vampire disguise visible
  instead of silently resembling a failed block.
- Saves, traits, IDs, NBT/ForgeCaps, payloads and TOML keys are unchanged.

## 1.1.14 - MCA editor and faction reconciliation

- Uses MCA 7.6.28's native per-eye dye API when present and disables the
  legacy full-head eye overlay on that MCA line. Vampire eyes now render in
  the updated editor without the black/red visual artifact, and the saved
  original eye dyes are restored when the trait is removed or cured.
- Treats the MCA editor trait set as an immediate combat invalidation boundary:
  removing Vampire, Hunter or Werewolf drops existing incompatible targets,
  clears stale retaliation memory and rejects a later invalid target assignment.
- Blocks rival-faction trade at MCA's direct interaction merchant call as well
  as the dialogue trade option. Unknown addon faction IDs now fall through to
  Vampirism's native helper/capability classification instead of being assumed
  human.
- Validated against MCA 7.6.16 / Vampirism 1.10.15 and MCA
  7.6.28-beta.10 / Vampirism 1.10.16. Saves, traits, IDs, NBT/ForgeCaps,
  payloads and TOML keys are unchanged.

## 1.1.13 - MerchantMenu startup hotfix

- Fixes the Forge 47.4.20 startup crash where `MerchantMenuMixin` shadowed
  the private `trader` field and the production refmap could not resolve it.
  The merchant is now captured from MerchantMenu's stable constructor, and
  Cycle Trades reads the public result slot instead of `MerchantContainer`.
- MCA merchant sessions now close through Forge's container-close event. The
  stored-Trader trade-sound protection remains intact without depending on
  private menu fields.
- Refmaps are generated only by MixinGradle and the release verification checks
  the packaged mixin, its single refmap entry and Forge 47.4.20's SRG
  `MerchantMenu` members.
- Validated both the original MCA 7.6.16 / Vampirism 1.10.15 baseline and the
  local MCA 7.6.28-beta.10 / Vampirism 1.10.16 combination. Saves, traits,
  IDs, NBT/ForgeCaps, payloads and TOML keys are unchanged.

## 1.1.12 - Compatibility audit hardening

- Routes MCA Cycle Trades exclusively through Easy Villagers' server C2S
  packet boundary. The compat no longer targets Easy Villagers' mixed GUI
  handler, which references client key bindings and is unsafe for dedicated
  server loading.
- The packet dispatcher handles stored Traders, active MCA faction menus,
  normal MCA menus and a validated MCA merchant-menu recovery path. Vanilla
  merchants and unrecognised wrappers continue through Easy Villagers' native
  handler. A handled cycle still receives exactly one Vampirism special-offer
  roll and never duplicates that offer.
- Adds binary API-contract checks for the supported Easy Villagers and MCA x
  EV bridge methods, explicit optional-integration diagnostics, and the
  extended local validation profile for Vampirism Integrations, Vampiric
  Ageing and Godly Vampirism.
- Saves, traits, IDs, NBT/ForgeCaps, network payloads and TOML keys are
  unchanged.

## 1.1.11 - MCA human blood-source hotfix

- Fixes Vampirism's native fangs not appearing for human MCA villagers when
  Vampirism Integrations is absent. MCA villagers now receive Vampirism's
  standard `ExtendedCreature` capability, so native target selection can see
  them as creature blood sources.
- Corrects the Forge 1.20.1 fallback NBT key from the incompatible
  `max_blood` spelling to Vampirism's actual `maxBlood` key. New human MCA
  sources initialize with 15 blood and use the standard bite/feed flow.
- MCA Vampires, Hunters and any already drained source retain their existing
  protection and eligibility rules. No trait ID, compat-state key, network
  payload or configuration value changed.

## 1.1.10 - MCA 7.6.28-beta.10 Hunter-attack startup hotfix

- Fixes the Forge startup crash caused by MCA 7.6.28-beta.10 delegating the
  MCA villager attack method to vanilla `Mob`. The legacy Hunter-hand redirect
  is now optional, and the delegated layout is covered at the stable vanilla
  attack boundary instead.
- A left-handed Hunter still uses its selected reserved weapon for native
  attacks on both MCA layouts. The known beta layout also retains the exact
  configured Hunter base damage rather than applying the legacy MCA guard
  multiplier compensation.
- No saved-state key, trait id, Forge capability, network payload or
  configuration value changed. The supported release baseline remains MCA
  7.6.16; this hotfix additionally handles the observed 7.6.28-beta.10 layout.

## 1.1.9 - Definitive Hunter weapon reservation

- A real Hunter now permanently reserves its selected pitchfork or stake in
  the managed hand. MCA recovery, work tasks and direct equipment-slot writes
  cannot produce an empty-hand packet or disarm an active Hunter.
- Hunter recovery consumes valid food directly from MCA inventory without
  moving it into the hand. Native food effects and container-item handling
  remain intact.
- Forge 1.20.1 Hunters with MCA's left-handed trait now use the reserved
  dominant-hand weapon in MCA's native attack lookup.
- No saved-state key, trait id, Forge capability, network payload or
  configuration value changed.

## 1.1.8 - Forge startup hotfix

- Fixes the runtime method selector used by the persistent Hunter hand guard.
  Version 1.1.7 can crash at Forge client startup because the selector used a
  development method name instead of Forge 1.20.1's runtime name.
- Keeps the persistent pitchfork/stake protection and recovery-food exception
  introduced in 1.1.7. No world data, trait ID, NBT key or configuration value
  changed; replace 1.1.7 rather than keeping both JARs installed.

## 1.1.7 - Hunter persistent-weapon fix

- A real Hunter's selected pitchfork or stake is now protected from direct
  hand-slot clears and replacements as well as MCA's EquipmentTask refresh.
  This removes the visible empty-hand/equip flicker and ensures native guard
  attacks use the equipped weapon.
- Recovery food remains explicitly allowed to replace the weapon for the
  normal MCA eating animation; the selected weapon returns when that use ends.
  No saved state, trait ID or configuration key changed.

## 1.1.6 - Forge equipment-mixin hotfix

- Fixes the Forge 1.20.1 runtime mappings used by the Hunter equipment
  redirect. Version 1.1.5 could fail while MCA loaded a villager because the
  redirect still referenced development method names. No world data is
  changed; replace 1.1.5 with this JAR before opening a world.

## 1.1.5 - Stable MCA Hunter equipment

- Prevents MCA's own equipment task from clearing or replacing the dominant
  hand of a real Hunter after the compat has equipped its persistent pitchfork
  or stake. This removes the client-visible empty-hand/equip flicker while
  preserving MCA armor, off-hand and active-item behavior.
- Skips vampire/werewolf cure, sunlight, shelter, growth and night-schedule
  work for Hunter-only runtime states. A newly applied conversion effect is
  still handled on the same tick.
- Moves detailed Cycle Trades diagnostics behind the existing debug setting,
  avoiding normal server-log spam during repeated rerolls.

## 1.1.4 - Packaging and maintenance refactor

- Moves GameTests and their structure out of the production artifact while
  retaining them in the development source set and source archive.
- Centralizes the bounded read-only NBT traversal used by the optional Easy
  Villagers bridge, preserving faction evidence precedence and diagnostics.
- Splits cure interaction and lifecycle entry points behind the unchanged
  public cure API, and isolates Forge 1.20.1 saved-hand ItemStack NBT.

## 1.1.3 - Villager runtime optimization

- Staggers the expensive faction/effect inspection for inactive MCA villagers
  to once per second; villagers with saved special state still process combat,
  curing, sunlight and conversion every tick.
- Replaces target-list stream sorting with a single-pass selector that computes
  each valid candidate's priority once while preserving category, faction,
  distance and tie ordering.
- Limits defensive-mode trait, goal and weapon reconciliation to staggered
  maintenance ticks. Expiration remains exact and combat remains responsive.

## 1.1.2 - Stable Hunter weapon ownership

- Fixes a conflict between temporary defensive mode and a real Hunter's
  persistent weapon. Real Hunters keep their selected pitchfork or stake while
  entering or leaving defensive mode; the temporary defense cleanup no longer
  restores the original hand and causes a visible equip/unequip loop.
- Gives permanent Hunter ownership priority over the temporary-defense source
  during weapon reconciliation. The persisted 75% pitchfork / 25% stake choice
  and low native attack damage remain unchanged.

## 1.1.1 - Villager Editor faction persistence

- Real MCA Hunters now attack through the native `Mob#doHurtTarget` flow,
  preserving normal damage sources, combat events, knockback, enchantment
  hooks and third-party combat-animation integrations. A transient modifier
  limits the base strike to `hunterVillagerAttackDamage` (2.0 by default), not
  the pitchfork's player-facing 9 damage.
- A real Hunter persists its first 75% pitchfork / 25% stake result; configure
  the stake share with `hunterVillagerStakeChance`. Old saves safely choose
  once on reconciliation. A custom `defensiveVillagerWeaponType` remains a
  fixed override and temporary defense remains unarmed unless its existing
  opt-in is enabled. Stakes do not grant advanced-Hunter instant executions.
- Fixes the MCA 7.6.16 Villager Editor Book restoring a stale `ForgeCaps`
  faction after a trait change. The completed server-side Editor packet now
  makes the saved Vampire, Hunter or Werewolf trait authoritative only after
  MCA and Forge have loaded the full entity NBT.
- A Vampire changed to Werewolf now clears the former vampire capability,
  appearance and trait before persisting the selected werewolf state. Removing
  every faction trait likewise persists a human state; faction traits remain
  mutually exclusive.
- Adds a GameTest that recreates the stale-Vampire-capability sequence and
  verifies the selected Werewolf trait survives the next runtime sync.

## 1.1.0 - Forge 1.20.1 release

This is the public release line for the Forge 1.20.1 port. It backports the
compatible MCA × Vampirism feature set from the Minecraft 1.21.1 / NeoForge
`2.x` line without changing 1.20.1 save, NBT, network or trait identifiers.

- Fixes the live MCA-menu Cycle Trades path when the original MCA menu-opening
  hook did not leave an active compat session. The canonical Easy Villagers
  GuiEvents boundary now validates the active MerchantMenu, recovers only its
  real VillagerEntityMCA, and immediately runs the unified MCA reroll instead
  of letting Easy Villagers clear the list natively.
- Keeps the packet hook as an active-session fast path, so an absent session
  reaches GuiEvents exactly once. Recovered Vampire, Hunter and Werewolf menus
  recreate their faction session; ordinary MCA menus recreate their native
  session. Vanilla villagers and incompatible merchant wrappers still remain
  under Easy Villagers control.
- Makes the optional Easy Villagers configuration bridge retry transient
  bootstrap lookups, accept ForgeConfigSpec.BooleanValue on 1.1.23–1.1.39,
  and retain an explicit loaded false as a hard opt-out. An unavailable
  bootstrap value cannot block an already-received GUI Cycle Trades action.
- Adds INFO diagnostics with session=OPENED or session=RECOVERED, and
  normalizes a pre-existing special offer's persisted result marker. The
  current-list marker is still cleared before every valid rebuild, which keeps
  each reroll independently eligible for exactly one native one-in-three roll.
- Adds GameTests for session-less Vampire and ordinary MCA merchant menus at
  the real GuiEvents boundary, including the stale-marker regression,
  duplicate prevention and vanilla fallback.

## Minecraft 1.21.1 / NeoForge reference history (`2.x`)

The `2.x` entries below describe the source feature history for the Minecraft
1.21.1 / NeoForge release line. They are reference history only; public Forge
1.20.1 artifacts use the `1.x` sequence, beginning with `1.1.0`.

## 2.1.12 - Unified Cycle Trades routing

- Routes every Easy Villagers Cycle Trades click through one active-session
  dispatcher: stored Trader first, faction-aware MCA menu second and ordinary
  MCA menu third. A live compat session no longer depends on the reflective
  Easy Villagers configuration lookup, so it cannot silently fall through to
  Easy Villagers' native reroller.
- Intercepts both `MessageCycleTrades.executeServerSide` and the canonical
  `GuiEvents.onCycleTrades` fallback boundary. The hooks are required on the
  supported Easy Villagers 1.1.23–1.1.39 range, but leave merchants without a
  compat session to Easy Villagers unchanged.
- Shares MCA offer reconstruction before the native one-in-three Vampirism
  roll. Each valid professional Vampire reroll clears the current-list marker,
  rebuilds the ordinary offers, performs exactly one roll and persists the
  result without duplicating a special offer. The stored-Trader empty-list
  fallback remains limited to its temporary vanilla entity.
- Adds INFO diagnostics for every consumed click, including its route,
  eligibility, roll result and final offer count; MCA/wrapper menus that
  arrive without an active session emit a WARN and fall back safely.
- Adds GameTest coverage for real `GuiEvents` routing in both stored Trader
  and normal MCA menus, native vanilla fallback and professional MCA offer
  rebuilds.

## 2.1.11 - Stored Trader offer-list fallback

- Fixes a remaining Easy Villagers Trader path where its temporary vanilla
  villager cannot regenerate MCA's serialized professional offers after Cycle
  Trades clears the list. The ordinary persisted offers are retained only in
  that empty-list fallback, while the preceding special offer is discarded and
  the new list receives its independent native one-in-three roll.
- Extends the stored-Trader GameTest to exercise this actual empty-regeneration
  condition before checking successful, failed and later successful rolls.

## 2.1.10 - Cycle Trades special-offer rerolls

- Fixes the Easy Villagers Trader Cycle Trades lifecycle for MCA Vampires. Each
  valid reroll now clears only the previous-list marker, rebuilds normal offers
  and makes exactly one new native one-in-three Vampirism special-offer roll.
- A failed roll no longer prevents later Cycle Trades actions from rolling
  again. A list can contain at most one converted-villager special offer, and
  the eligibility lock still activates permanently after the first real trade.
- Shares the same explicit reroll boundary with regular MCA merchant menus and
  emits rebuilt-list, roll and result diagnostics when compat debug logging is
  enabled.
- Resolves Easy Villagers' runtime `m_35540_` price-update method in addition
  to the development `updateSpecialPrices` name, removing the release-side
  `NoSuchMethodException` warning.

## 2.1.9 - Production MerchantMenu refmap repair

- Fixes the production refmap entries for `MerchantMenu.trader` and
  `MerchantMenu.tradeContainer`. Release JARs can now apply the faction-trade
  menu mixin under Forge's runtime mappings instead of aborting during startup.

## 2.1.8 - Easy Villagers 1.1.39 Compatibility

- Validates the optional Easy Villagers bridge, its Trader hooks and Cycle
  Trades state with Easy Villagers 1.20.1-1.1.39 while retaining coverage for
  1.20.1-1.1.23.
- Expands the optional Forge metadata range to the tested inclusive
  1.20.1-1.1.23 through 1.20.1-1.1.39 range, preventing a false mod-loading
  rejection for 1.1.39.

## 2.1.7 - Forge 1.20.1 backport

- Delivers the 2.1.7 compatibility behavior on Minecraft 1.20.1, Forge
  47.4.20 and Java 17 with MCA Reborn 7.6.16 and Vampirism 1.10.15.
- Restores the 1.20.1 Forge capability/NBT persistence and SimpleChannel
  transport while preserving the 1.0.3 capability key and legacy faction
  trait ids for existing worlds.
- Keeps Werewolves 2.0.2.7, Easy Villagers 1.1.23 and mca-ev-compat 1.0.2
  optional. The Easy Villagers bridge intentionally excludes Inventory Viewer
  and other integrations that only exist on the 1.21 line.
- Restores Forge 1.20.1 data-pack paths (`advancements`, `recipes`,
  `structures` and `tags/items`) so the guide recipe, progress, GameTest
  structure, bookshelves and faction gift tags load on the target runtime.

- Moves the native Vampirism special-offer roll to MCA's own `getOffers`
  generation boundary. Professional MCA Vampires are now considered whenever
  MCA actually produces its live offer list, including a Cycle Trades reroll
  and a profession refresh.
- Keeps the menu-opening call only as a safe fallback. The roll remains native:
  one special Vampirism offer has a one-in-three chance per generated list.

## 2.1.6 - Startup repair for special Vampirism offers

- Fixes a startup crash in 2.1.5 caused by an invalid inherited-method Mixin
  accessor on `Villager`.
- Uses the public `ItemListing#getOffer` API to select the native Vampirism
  converted-villager offer directly, preserving the same one-in-three chance
  without an additional Mixin.

## 2.1.5 - Vampirism special offers for MCA vampires

- MCA villagers converted to Vampires now use Vampirism's native one-in-three
  special-offer roll when their profession offers are generated. This can add
  the same heart, emerald and blood-bottle opportunities offered by native
  converted Vampirism villagers.
- Keeps MCA's live offers and real merchant transaction intact; faction prices
  still apply only as the temporary session overlay.
- Re-rolls the special-offer opportunity together with Cycle Trades and
  persists the outcome while the same offers remain active, so reopening a
  merchant cannot duplicate special offers.
- Makes the same offer pool available in the Easy Villagers Trader and removes
  compat-added special offers when a Vampire is cured or edited back to human.

## 2.1.4 - Active Hunter patrols

- New installations let Hunter-aligned MCA villagers actively target revealed
  Vampire players, instead of limiting their faction combat to defensive
  situations.
- Reduces the default Hunter melee damage from 3.0 to 2.0 (one heart) per
  hit, before normal Minecraft damage modifiers.
- Keeps Vampire disguises safe by default and never targets players who are no
  longer Vampires.
- Existing TOML files are intentionally preserved. Set
  `hunterVillagersAttackOnlyWhileDefensive=false` and
  `hunterVillagerAttackDamage=2.0` to opt into the new defaults.

## 2.1.3 - Player attacks are no longer bites

- Stops treating generic player melee damage as Vampirism blood feeding.
  Sword, fist and Better Combat hits now retain their normal damage against
  MCA villagers.
- Leaves infection, blood drinking and Hunter toxicant exclusively to
  Vampirism's native bite and blood-drinking flows.
- Keeps the two legacy player-bite damage TOML keys readable but marks them as
  obsolete, so existing configuration files remain valid.

## 2.1.2 - Stability and localisation polish

- Resolves active village-mission residents directly by UUID rather than
  querying the full editable MCA village area every 40 ticks.
- Clears temporary village, outline and inherited-birth state when a server
  stops, preventing stale integrated-server references.
- Bounds diagnostic message cooldown caches used by capture, commands and
  Hunter blood handling.
- Completes the pt-PT translations and adds a resource test that keeps all
  shipped locales aligned with the English key set.

## 2.1.1 - Trait display names

- Restored the player-facing names and descriptions of the Vampire, Hunter and
  Werewolf traits on MCA 7.7.36, whose ResourceLocation trait registry now
  uses dot-separated translation keys.

## 2.1.0 - MCA 7.7.36 Compatibility

- Ports faction traits to MCA's ResourceLocation trait registry while preserving canonical and legacy trait ids.
- Migrates faction trading and Cycle Trades to MCA 7.7.36's native Villager merchant flow, preserving real-offer transactions, reputation, Hero of the Village and temporary faction prices.
- Requires the coordinated mca-ev-compat 2.1.0 update when Easy Villagers is installed; older 2.0.0 builds target removed MCA trade methods.

## 2.0.43 - MCA Identity Protection from Native Werewolves

- Prevents Werewolves' native villager mark, full-moon, rage and time-limited transformation paths from replacing an MCA villager with a beast entity.
- Removes only the legacy native `werewolves.werewolf` marker from MCA villager saves, without fabricating a compat werewolf trait or changing legitimate bite, inheritance, editor or cure state.
- Preserves the MCA villager's UUID, name, profession, offers, relationships, genetics, appearance and captured-villager data when a native transformation is blocked.
- Leaves vanilla Werewolves entities, native population replenishment and all explicit compat werewolf conversions untouched.
- Restores quoted MCA display-name resolution for the existing OP `inspect` and `resync` commands.

## 2.0.42 - Faction Gifts and Recovery Diets

- Adds configurable faction gift tastes while retaining MCA's native GiftSaturation, so favorite gifts cannot be repeated indefinitely.
- Keeps enemy-faction gifts blocked, with disguise and established faction rules still deciding access.
- Makes a valid garlic injection reduce the recipient's relationship with its applier by 10 MCA hearts, exactly once per accepted cure ingredient.
- Gives Vampires blood-bottle recovery and Werewolves a meat-only recovery diet; Humans and Hunters retain MCA's normal food recovery.

## 2.0.41 - Native Village Favor Conversation

- Sends the resident's village-favor lines through MCA's native chat presentation while keeping only the reply choices in the MCA conversation screen.
- Ends the MCA conversation safely after accepting, declining, checking or abandoning a favor, without closing an unrelated screen on delayed packets.
- Rewrites the favor dialogue, action-bar notices and Book of Occult Arts page in Brazilian Portuguese, European Portuguese and English without changing mission rules or saved data.

## 2.0.40 - Legacy Village Favor Recovery

- Recovers unanswered pre-conversation village requests that were invisibly bound to a different resident.
- The first compatible resident approached through MCA conversation adopts that legacy request, preserving its item and quantity while starting no deadline until the player accepts.
- New conversation-native requests remain bound to the resident who offered them.

## 2.0.39 - MCA Conversation Village Favors

- Moves every village favor into MCA's native conversation with the resident who needs help.
- Removes automatic chat invitations, reminder spam and player mission commands.
- Starts the deadline only when the player accepts a favor; progress and cancellation are handled by speaking with the same resident again.
- Preserves existing accepted and pending mission records, including their target, quantity, deadline and personal outline.

## 2.0.38 - Compact Village Mission UI

- Reworks village-mission presentation so the chat contains only one compact, clickable request line.
- Moves accepted-request feedback, reminders, completion, cancellation and expiry notices to the temporary action bar.
- Keeps the cancel button available in chat immediately after a request is accepted, while avoiding repeated chat clutter.
- Shortens all village-mission translations in Brazilian Portuguese, European Portuguese and English without changing mission rules or saved data.

## 2.0.37 - Village Mission Dialogue Polish

- Rewrites every village-mission invitation, acceptance, reminder, cancellation and expiry message as faction-themed dialogue in Brazilian Portuguese, European Portuguese and English.
- Puts the remaining time naturally into each resident's line instead of sending a detached technical deadline message.
- Updates the Village Favors book page and mission advancement descriptions to match the player-facing wording.
- Keeps legacy active missions valid without fabricating a deadline or sending misleading timed reminders.

## 2.0.36 - Village Mission Deadline Fix

- Fixes Crimson Reserve missions: a full Vampirism Blood Bottle now uses the actual full-bottle value, so separate filled bottles anywhere in the inventory can be delivered together.
- Adds the configurable `occultVillageMissionDeadlineTicks` deadline, set to two Minecraft days by default for new village requests.
- Lets players cancel accepted requests with `/mca_vamp_compat mission cancel` or the new chat button. Canceled and expired requests start the normal village cooldown before another request can appear.
- Keeps existing active requests valid after upgrading; only newly assigned requests receive a deadline.

## 2.0.35 - Legacy Snapshot Cleanup

- Repairs empty legacy item compounds before MCA or mca-ev-compat decodes stored villager snapshots.
- Stops the repeated `No key id in MapLike[{}]` client warnings caused by old empty main-hand, armor, hand and inventory entries.
- Preserves the saved empty-hand marker, all valid items, MCA data, faction payloads, traits and Easy Villagers owner snapshots.

## 2.0.34 - Village Favor Polish

- Village supply quantities are now selected from fair per-request ranges and saved with the active request.
- Highlights mission quantities in bold white text in requests, reminders, and insufficient-supply feedback.
- Lets the Crimson Reserve request consume its full blood bottles across the player's inventory instead of requiring an unsafe stackable bottle item.
- Reminders now name the exact supply or urgent aid still needed.
- Keeps existing mission records valid: active requests saved before 2.0.34 retain their former fixed quantities.

## 2.0.33 - Village Favor Invitations

- Makes every village favor an explicit player choice. The normal chat invitation now has localized Accept and Decline buttons; declining starts the same village cooldown without a reward.
- Highlights the accepted MCA resident only in the accepting player's client, without changing synced entity data or exposing the outline to other players.
- Rewrites favor dialogue in a more natural in-world voice, highlights the requesting villager's name in chat and changes Crimson Reserve to require ordinary Vampirism Blood Bottles.
- Keeps existing 2.0.32 active missions completable on upgrade; they are treated as already accepted.

## 2.0.32 - Occult Village Favors

- Adds optional, localized supply and aid requests from eligible MCA residents of active Vampire, Hunter and Werewolf villages.
- A player can finish one request per village totem during the configurable cooldown. Matching MCA gifts consume the requested amount exactly once and award five relationship hearts only with the resident who asked.
- Adds Hunter emergency aid: curing the assigned human MCA resident's confirmed Vampirism infection with milk completes the request.
- Stores only per-player mission progress in SavedData, uses existing 40-tick special-village detection, and never changes villager NBT, traits, UUIDs, commerce, capture or AI.
- Adds four Arts Occultes advancements and page 31 of the Book of Occult Arts.

## 2.0.31 - Refusal Message Cleanup

- Removes the visual refusal icons from faction trade, Easy Villagers pickup and specialist-workstation messages.
- Restores the original localized messages without changing faction rules, prices, capture behavior or advancement progress.

## 2.0.30 - Occult Arts Advancements and Refusal Icons

- Adds the Arts Occult tab to Minecraft's advancement screen, with progression for faction infection, conversion, trade discounts, curing, inherited children, compatible pickup and specialist workstations.
- Adds simple bitmap refusal indicators: a red eye for Vampires, golden garlic for Hunters and a silver paw for Werewolves. They accompany existing blocked trade, pickup and specialist-station messages.
- Hides Werewolves and Easy Villagers advancement leaves when the corresponding optional integration is absent, and safely credits attributable conversion/cure progress after a player logs back in.

## 2.0.29 - Unlimited MCA Village Bounds and Fog Margin

- Removes the artificial horizontal cap from MCA-derived compat areas: Hunter control, Occult Arts discovery and the diagnostic outline now use the complete editable MCA village footprint.
- Adds `mcaVillageFogBorderMarginBlocks`, defaulting to four blocks, to extend only active Vampire village fog beyond the MCA border.
- Keeps the logical village boundary unchanged for gameplay and diagnostics, synchronizes the expanded visual fog to clients, and reapplies it after common-config reloads.

## 2.0.28 - Release Validation Polish

- Makes the Werewolf specialist-station GameTest conditional on the optional Werewolves mod so every supported dependency profile can pass its own test matrix.
- Skips Easy Villagers and mca-ev-compat mixins before Mixin resolves absent optional targets, removing harmless missing-class warnings from optional profiles.
- Corrects the README to document the real-offer temporary price overlay used by faction trading since 2.0.13.

## 2.0.27 - MCA Villager Name Commands

- Makes `/mca_vamp_compat inspect` and `resync` resolve the visible name of loaded MCA villagers instead of treating it as an online player name.
- Keeps UUID and entity-selector inputs, adds clear feedback for missing or duplicate MCA names, and supports quoted names with spaces.

## 2.0.26 - Client-Synchronized MCA Village Fog

- Synchronizes the MCA-derived Vampire fog footprint in the normal totem update packet, so the client reapplies the exact editable MCA village boundary after Vampirism loads its native totem data.
- Sends an immediate totem update after MCA changes a village's dimensions; fog now grows and shrinks visually with the same bounds used by the administrative outline, Hunter control and book discovery.
- Keeps native Vampirism fog as the fallback for inactive/non-Vampire totems or totems outside an MCA village, and rejects malformed client payloads safely.

## 2.0.25 - Exact Village Fog Bounds and Command Help

- Makes `/mca_vamp_compat` and `/mca_vamp_compat village` show a concise OP command guide when run without a subcommand.
- Corrects the horizontal exclusive/inclusive conversion between MCA village bounds and Vampirism's `LevelFog`, so the fog stops on MCA's final building block instead of extending one block beyond it.
- Keeps the logical MCA area unchanged for Hunter control, Occult Arts discovery and the diagnostic outline, while `village info` now also reports the precise block range sent to the fog system.

## 2.0.24 - Administrative Tools and Faction Dialogue

- Adds the OP level-2 `/mca_vamp_compat` command with read-only villager inspection, safe lifecycle resync and effective-village area information.
- Adds a private, particle-only temporary outline for the effective special-village area. It uses the same bounds as fog, Hunter control and Occult Arts discovery, refreshes every 10 ticks and is capped for safety.
- Adds `enableFactionContextualDialogues`, enabled by default. Short localized faction lines now accompany accepted MCA greetings, existing faction trade/social refusals and cure start/completion without changing relationships, prices, commands or combat.
- Restores missing source resources and the three existing source classes from the local 2.0.23 source artifact so clean builds remain reproducible.

## 2.0.23 - MCA Editor Faction Traits

- Makes the MCA villager editor's Vampire, Hunter and Werewolf traits immediately apply their complete compat state after saving.
- Removing one of those traits now clears its matching conversion, appearance, attributes, cure/infection progress and Hunter pitchfork state instead of leaving an orphaned faction state.
- Keeps faction states mutually exclusive when an edited trait set contains more than one special faction.

## 2.0.22 - Trader Specialist Workstation Refinement

- Stops client-side prediction for the three faction-specialist stations in an Easy Villagers Trader, preventing temporary insertion or inventory flicker.
- Uses a consumed server interaction for incompatible factions, so the held block is neither consumed by the Trader nor placed in the world as a fallback.

## 2.0.21 - Trader Client Crash Hotfix

- Makes faction-specialist workstation validation server-authoritative in Easy Villagers Trader.
- Prevents the client-side click prediction from resolving stored villager offers, fixing the `Cannot load Villager offers on the client` crash.
- Keeps compatible workstation assignment and incompatible-faction refusal unchanged on the server.

## 2.0.20 - Faction Specialist Workstations

- Adds the Altar of Inspiration, Hunter Research Table and Stone Altar as alternate workstations for Vampire Expert, Hunter Expert and Werewolf Expert, respectively.
- Preserves the original Vampirism and Werewolves faction totems as valid job sites and leaves the Priest's Altar of Cleansing path unchanged.
- Requires the matching MCA faction for the three new world workstations, releasing a rejected station so an eligible villager can claim it.
- Extends Easy Villagers Trader's native workstation placement: an incompatible stored MCA villager refuses the block before it is consumed, while a compatible villager receives the specialist profession normally.

## 2.0.19 - Immediate MCA Village Bounds Updates

- Replaces the five-second MCA village bounds polling with immediate refreshes from MCA's own village geometry updates.
- Covers player edits, automatic scans and village merges through `Village.calculateDimensions()`, plus final-building removal through `Village.removeBuilding(int)`.
- Refreshes only totems that were linked to the changed MCA village or are newly inside its recalculated footprint.
- Keeps fog restoration, Hunter alignment reconciliation and all native Vampirism capture, raids, POIs and spawning behavior unchanged.

## 2.0.18 - MCA Village Effect Bounds

- Makes vampire village fog follow the editable MCA village footprint when the faction totem is located inside that MCA village.
- Uses the same effective footprint for Hunter village alignment and Book of Occult Arts discovery, while keeping native Vampirism capture, raids, POIs and spawning untouched.
- Adds `mcaVillageEffectsFollowMcaBounds` and the 256-block-per-direction safety cap `mcaVillageEffectsMaxHorizontalRadius`.
- Re-evaluates loaded totems every five seconds and immediately after totem changes, restoring the normal Vampirism fog when no MCA village applies.

## 2.0.17 - Vampire Night Schedule

- Gives adult MCA vampire villagers MCA's native nocturnal routine: rest and sleep during the day, then return to their profession at night.
- Preserves explicit MCA commands and combat, coordinates bed travel with sunlight sheltering, and restores the prior MCA schedule after cure, capture/restore, growth or role changes.
- Adds `vampireVillagersUseNightSchedule`, enabled by default, and updates the Occult Arts guide's sunlight page.

## 2.0.16 - Cycle Trades Audit Hotfix

- Restores MCA's native reputation and Hero of the Village offer preparation after an ordinary MCA villager rerolls trades.
- Shares the cached MCA offer-preparation bridge between ordinary and faction-aware merchant sessions.
- Keeps the permanent Cycle Trades lock authoritative in the completed native-trade event, removing redundant session writes.
- Updates release documentation and build examples to 2.0.16.

## 2.0.15 - Placed Guard Cycle Trades Hotfix

- Restores the mca-vamp-compat payload in mca-ev-compat's real item-placement path before the MCA villager joins the world.
- Restores the saved canonical faction trait before MCA initializes the placed villager, preserving manually assigned Vampire, Hunter and Werewolf behavior after capture and placement.
- Routes ordinary MCA merchant screens, including Guards with no faction, through the server-authoritative Cycle Trades state. Profession XP is ignored: only a completed offer, its persisted lock or a legacy used offer can disable the control.
- Keeps an untraded Guard eligible after capture and placement, while a Guard that has completed a real trade remains permanently locked.

## 2.0.14 - Cycle Trades Capture Hotfix

- Stops treating MCA profession XP as proof that a villager has traded, so a professional villager that has never traded can still use Cycle Trades after Easy Villagers capture and placement.
- Keeps the permanent post-trade lock authoritative through the existing persistent marker, with used offers retained as a fallback for older saved villagers.

## 2.0.13 - Faction Trade Transaction Hotfix

- Restores MCA's native merchant transaction for live faction villagers, so Shift-click consumes payment, grants output and records uses from the same offer instance.
- Applies faction prices as a temporary overlay after MCA has prepared reputation and Hero of the Village prices, then restores the underlying offer values when the screen closes.
- Uses the same real-offer model for Easy Villagers Trader sessions and persists their base prices, uses, XP and restock state without saving a temporary faction discount.
- Keeps Hunter, Vampire and Werewolf refusals, discounts, disguise rules and Cycle Trades behavior unchanged while making the first-trade lock follow the native completed trade event.

## 2.0.12 - Hunter Pitchfork Equipment Task Hotfix

- Reapplies a valid Hunter's pitchfork immediately after MCA's `EquipmentTask` refreshes the villager's dominant hand, preventing the visual hand flicker most visible on villagers with active trades or professions.
- Leaves MCA recovery-food use untouched until the villager has finished consuming the temporary item.
- Keeps original-hand preservation, faction state, trade offers and Easy Villagers snapshots unchanged.

## 2.0.11 - Capture, Pitchfork and Cycle Trades Hotfix

- Safely serializes a saved empty main hand, so an allied Hunter whose temporary pitchfork replaced an empty hand can be captured, saved and restored without losing data.
- Reads old empty-hand state safely and reports permitted pickup failures clearly when the inventory is full, the villager is sleeping or a snapshot cannot be created.
- Centralizes Hunter pitchfork reconciliation so it is equipped only while a valid Hunter/defensive source exists and the original hand is restored once when that source ends.
- Makes the Cycle Trades lock authoritative and persistent after the first trade for both MCA villagers and Easy Villagers Trader sessions; the button is disabled immediately and later reroll requests are ignored.
- Keeps pre-trade rerolls safe by resetting the selected offer before the new offer list is sent.

## 2.0.10 - Merchant Wrapper Trade Completion Hotfix

- Fixes a `ClassCastException` when Shift-clicking a result in faction-aware merchant menus.
- Routes the vanilla trade confirmation sound through the real MCA/stored villager instead of casting the temporary merchant wrapper to an entity.
- Applies the fix to live MCA vampire/hunter/werewolf trades and Easy Villagers Trader-block sessions.
- Adds an integration regression test that completes a discounted Hunter trade by Shift-click and verifies the use on the real stored offer.

## 2.0.9 - Cycle Trades for Every Faction and Release Polish

- Makes Easy Villagers' Cycle Trades control available in every faction-aware merchant wrapper, including vampire, hunter and werewolf MCA villagers.
- Keeps the original Easy Villagers feature toggles, artwork, position and input handling while routing rerolls to the real villager behind the wrapper.
- Allows faction villagers whose MCA/merchant metadata exposes progress or experience to use the control instead of being incorrectly treated as ineligible humans.
- Removes inactive legacy pickup settings, obsolete eye-overlay code, unused future toggles and the unregistered Vampirism Integrations mixin.
- Makes the mca-ev-compat state bridge automatic whenever that optional integration is available, preserving the owner snapshot and legacy world migration.
- Refreshes release metadata, configuration documentation, CurseForge copy and distributable checksums.

## 2.0.8 - Easy Villagers Cycle Trades

- Restores Easy Villagers' native Cycle Trades button on faction-aware merchant wrappers.
- Routes the cycle packet to the real stored villager instead of letting Easy Villagers discard the wrapper merchant.
- Regenerates and persists the stored villager's offers while preserving faction refusals, reputation modifiers and faction discounts.
- Keeps the feature optional and honors Easy Villagers' server and client settings, novice-only restriction and original button artwork.

## 2.0.7 - Authoritative Hunter Trade Fix

- Routes MCA's direct right-click trade path through the same Hunter refusal and price wrapper used by its dialogue command.
- Intercepts the concrete Easy Villagers Trader block before its original GUI can open, while retaining the tile-entity hook as a fallback.
- Validates both authoritative entry points against real MCA Hunter traits and mca-ev-compat visual snapshots.

## 2.0.6 - Book Arrow and Author Style Fix

- Places the four supplied page-turn sprites in MCA's exact 23×13 atlas cells.
- Makes Seleris' title-page credit regular italic instead of inheriting the title's bold style.

## 2.0.5 - Book of Occult Arts Visual and Content Refresh

- Replaces the item icon and page atlas with the supplied purple artwork.
- Uses a centered, wrapped title page with the correct author and readable dark colors.
- Replaces all 30 pages with the revised player-facing guide in Brazilian Portuguese, European Portuguese and English.
- Scales only pages that exceed MCA's native 14-line area so no text is clipped.
- Removes the redundant main-hand tooltip while preserving the safe offhand warning.

## 2.0.4 - Stored Hunter Trades and Occult Arts Guide

- Reads the authoritative 1.21.1 mca-ev-compat visual snapshot component when classifying villagers stored in Trader blocks.
- Recognizes MCA 7.7 canonical faction traits in their real boolean-key NBT format, including old v2 payloads whose `Hunter=false` flag previously hid a true Hunter trait.
- Treats false legacy payload flags as missing evidence so canonical MCA faction traits remain authoritative.
- Writes mutually exclusive version 3 faction payloads from both synchronized state and canonical traits.
- Validates the exact Easy Villagers `openTradingGUI(Player)Z` hook and replaces the direct Trader GUI before offers are opened.
- Adds the MCA-style Book of Occult Arts with a player-facing guide to factions, infection, curing, inheritance, villages and Easy Villagers integration.
- Gives the guide once when a player discovers an active vampire, hunter or werewolf-controlled village and unlocks its replacement recipe.

## 2.0.3 - Stored Hunter and Werewolf Inheritance Fixes

- Prioritizes the mca-ev-compat visual villager snapshot for stored faction classification.
- Keeps inherited werewolf children valid when child infection by bites is disabled.
- Reads Breeder parents from their visual snapshots and preserves inherited state through Incubator.

## 2.0.2 - Trader, Sunlight and Inheritance Fixes

- Intercepts the Easy Villagers Trader tile directly so Hunter trade blocks and faction discounts cannot be bypassed by its original GUI.
- Replaces the conflicting vanilla flee-sun goal with MCA Brain `WALK_TARGET` shelter navigation.
- Applies faction inheritance in MCA's actual `Pregnancy.createChild` path, which does not fire NeoForge's `BabyEntitySpawnEvent`.
- Persists inheritance decisions through MCA Baby Items and the mca-ev-compat Breeder/Incubator item flow.
- Adds werewolf inheritance chances: 45% for one werewolf parent and 100% for two.
- Makes inheritance idempotent and mutually exclusive between vampire and werewolf state.

## 2.0.1 - NeoForge 1.21.1 Compatibility Hotfix

- Fixed MCA villagers with uninitialized Vampirism blood data being impossible to drain (`max_blood` now uses the correct 1.21.1 key).
- Fixed inherited upper-case faction traits that could make otherwise human newborns count as vampires.
- Migrates the accidental 2.0.0 upper-case trait ids to the canonical legacy namespace without converting human villagers.
- Uses MCA 7.7's native eye color, brightness and skin genetics instead of obsolete face overlays and late `SynchedEntityData` fields.
- Vampire eyes are native bright red; vampire skin is derived one MCA tone lighter from the saved original appearance.
- Fixed the Villager Editor crash caused by querying trade offers on the logical client.
- Added Brazilian and European Portuguese migration-safe trait translations.

## 2.0.0 - NeoForge 1.21.1 Port

- Ported the project to Minecraft 1.21.1, Java 21 and NeoForge 21.1.234.
- Updated MCA packages and hooks for MCA 7.7.25+1.21.1 and Vampirism 1.10.12.
- Replaced Forge capabilities with a serializable NeoForge attachment while retaining the legacy `vampirism_mca_compat` trait namespace.
- Added automatic migration of the 1.0.3 `ForgeCaps` state without replacing MCA villagers.
- Updated synced entity data, damage/tick events, registries, effects, render buffers and item data components for 1.21.1.
- Preserved vampire, hunter, werewolf, infection, cure, AI, combat, sunlight, guard, trade, social, command and village-capture rules.
- Added a versioned `mca_vamp_compat_state` Easy Villagers payload and payload-first faction resolution.
- Added optional save/restore hooks for mca-ev-compat 2.0.0; the owner snapshot remains untouched.
- Disabled unvalidated Vampirism Integrations mixins; core blood handling remains available without that add-on.
- Added unit tests for state round-trip, 1.0.3 migration and Easy Villagers item payload classification.

## 1.0.3 - Red Eyes, Blood Values and Stability Cleanup

- Stabilized local ForgeGradle deobfuscation for optional mod jars to avoid inherited vanilla methods being emitted with unmapped names.
- Hotfix: removed the temporary `VillagerEntityMCA` vanilla bridge mixin used during testing.
- Hotfix: restored visual synced-data code to the stable runtime path and fixed the production owner mapping through the build.
- Hotfix: removed a fragile `Mob` goal selector accessor that could crash during mixin apply in production.
- MCA vampire villagers now use MCA's native red custom eye face overlays by default.
- The default MCA red-eye mode chooses a stable per-villager variant from face indices `1`, `8`, `12` and `19`.
- Custom red eyes are supplied through MCA's own normal `FaceLayer`, so the normal face layer does not cover them.
- Added `vampire.useMcaRedEyesForMcaVampires`, default `true`.
- Changed `vampire.useVampirismEyesForMcaVampires` default to `false`; enabling it keeps the previous Vampirism eye overlay behavior.
- The normal MCA face layer is only hidden when the Vampirism eye overlay is active.
- The new eye rendering is client-side only and does not alter MCA genetics, NBT, traits, family or relationship data.
- Added MCA villager blood support without requiring Vampirism Integrations.
- When Vampirism Integrations is installed, its own MCA blood values are used; this mod no longer ships duplicate `bloodvalues` JSON.
- Without Vampirism Integrations, MCA male and female villagers use a runtime fallback blood value of `15` through Vampirism's core `ExtendedCreature` capability.
- Added a small repair for MCA villagers whose Vampirism blood capability was created or loaded without valid blood.
- Player vampire blood drain uses the standard Vampirism core bite/feed flow, including the normal UI and feedback.
- Vampirism Integrations is no longer required just to make MCA villagers valid blood sources.
- Existing blocks for MCA vampire targets and hunter poisonous blood remain in the same event flow.
- Sanguinare infection behavior for valid player bites is preserved.
- Runtime behavior remains aligned with 1.0.2.
- No gameplay, balance, AI, curing, trade/social, pickup, command or capture rules were changed.

## 1.0.2 - Guard Defense / Release Polish

- Added configurable guard-defense exception for MCA guards that are vampire, hunter, werewolf or defensive villagers.
- MCA special guards now keep defending villages against common hostile mobs.
- Works for vampire, hunter and werewolf MCA guards.
- Normal special villagers still ignore common monsters as before.
- Common hostile mobs may target special MCA guards when guard defense is enabled, so guard combat is not silently canceled by monster-ignore.
- Villager panic/flee behavior remains suppressed for special villagers.
- Creeper auto-targeting is disabled by default through `monster_behavior.specialMcaGuardsAttackCreepers=false`.
- Added/updated configs for special guard defense behavior.
- Final release polish, config comments and documentation cleanup.
- Follow/stay command respect, trade/social, curing, pickup, Easy Villagers delegation and village capture behavior were not changed.

## 1.0.1 - Release cleanup

- Public mod identity changed to `MCA Reborn x Vampirism Compat`.
- Public mod id changed to `mca_vamp_compat`.
- Final jar name changed to `mca-vamp-compat-1.20.1-1.0.1.jar`.
- License set to `GPL-3.0-only`.
- Final release cleanup removed old local `javap` investigation dumps.
- README feature text was rewritten for release/CurseForge usage.
- Configured categories reorganized into broader release-facing sections: `vampire`, `hunter`, `werewolf`, `factions`, `easy_villagers`, `mca_commands`, `village_capture`, `monster_behavior`, `balance`, `debug`, and `advanced`.
- Added `balance.specialVillagersTargetFactionNpcs`, default `false`, so MCA special villagers do not automatically start faction wars with non-MCA Vampirism/Werewolves NPCs by default.
- Polished special villager movement: normal MCA navigation is preserved outside combat, combat chase speed is moderated, and default base movement multipliers stay close to normal MCA villagers.
- Polished combat movement/focus: lowered chase defaults, added combat repath cooldown, and forced look control toward the active combat target while the combat goal is running.
- Faction-aware Easy Villagers pickup permissions are enabled while base pickup/restore/render/NBT handling remains delegated to mca-ev-compat.
- Faction-aware MCA command permissions are enabled and special AI respects MCA follow/stay.
- Vampire/werewolf curing, werewolf support, price stacking, trade/social rules, monster behavior and safe village capture are included in the release feature set.
- Existing gameplay defaults from the balance polish were preserved.
- No gameplay rules were changed in the final cleanup pass.
