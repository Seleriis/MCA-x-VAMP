# Auditoria focada — 2.0.35

Data da auditoria: 13 de agosto de 2026.

## Objetivo

Validar a correção preparada para snapshots legados com itens vazios e
inspecionar os caminhos de maior risco sem alterar mecânicas, configurações
ou formatos de mundo.

## Resultado da correção de snapshots

Os logs fornecidos continham 216 ocorrências de:

```
Tried to load invalid item: No key id in MapLike[{}]
```

O padrão é compatível com snapshots antigos que codificavam uma pilha vazia
como `{}`. Em 1.21.1, essa estrutura não pode ser lida como `ItemStack`.

A correção atual é deliberadamente restrita:

- remove compostos vazios somente de `OriginalMainHandItem` e
  `OriginalOffHandItem`;
- substitui entradas vazias de `HandItems` e `ArmorItems` por `minecraft:air`,
  preservando a posição do equipamento;
- remove entradas vazias de listas de inventário (`Inventory` e `Items`);
- preserva compostos vazios que não representam itens;
- executa apenas durante a sanitização, hidratação, restauração ou prévia de
  snapshots do Easy Villagers; não existe varredura por tick.

O mixin opcional cobre os dois caminhos do `mca-ev-compat` que chegam ao
MCA: cópia/sanitização do snapshot e preparação para hidratação. A correção
também regrava apenas o payload pertencente a este compat quando uma pilha já
está sendo normalizada pelo `mca-ev-compat`.

Evidências estáticas:

- `LegacyEmptyItemStackSanitizer` compilou isoladamente contra o runtime
  Minecraft 1.21.1 resolvido pelo Gradle;
- a fixture cobre mão original vazia, equipamento posicional, inventário e
  compostos não relacionados;
- os três métodos-alvo do `CompatLogic` foram confirmados no JAR
  `mca-ev-compat` 2.0.0;
- os 24 mixins comuns e 4 mixins de cliente declarados possuem arquivos-fonte.

## Fluxos revisados

| Área | Resultado estático | Observação |
| --- | --- | --- |
| Comércio e Cycle Trades | Sem inconsistência nova encontrada | O comércio MCA usa a oferta real com sobreposição temporária de preço; a sessão EV restaura o preço antes de persistir. |
| Captura e restauração EV | Sem inconsistência nova encontrada | O caminho de snapshot é justamente onde a limpeza legada é aplicada. |
| Herança e cura | Sem inconsistência nova encontrada | Eventos de nascimento e entrada de entidade são filtrados no servidor. |
| Forcado e rotina noturna | Sem inconsistência nova encontrada | Reconciliados na rotina MCA já existente, sem tarefa paralela nova. |
| Limites de vila e névoa | Sem polling de área | A atualização continua orientada a eventos do MCA; o contorno tem sessão temporária. |
| Cliente, servidor e opcionais | Sem ligação obrigatória indevida encontrada | Mixins Easy Villagers e `mca-ev-compat` são filtrados pelo plugin opcional; estados de cliente não referenciam classes de renderização no servidor. |

## Achados

### Validação de build — concluída

A primeira tentativa foi interrompida quando o NeoForge não conseguiu reabrir
um JAR temporário (`AccessDeniedException` em
`build/tmp/neoformruntime/.../recompile/output.jar`). A execução fora do
sandbox concluiu corretamente após isso:

```
gradlew.bat releaseBundle --no-daemon
BUILD SUCCESSFUL
59 testes concluídos, 0 falhas
```

O pacote 2.0.35 foi gerado e a presença do sanitizador e do mixin opcional foi
confirmada no JAR final. GameTests e smoke tests manuais continuam sendo
cenários de jogo a validar antes de uma publicação pública.

### Correção recomendada — classe obsoleta de fuga do sol

`ai/McaVampireSeekShelterGoal` ainda estende `FleeSunGoal`, mas não possui
nenhuma referência de criação ou registro. A proteção atual usa o controlador
compatível com o Brain do MCA.

Proposta: remover essa classe em uma etapa aprovada de limpeza. Não altera
dados nem gameplay, pois ela não participa da execução atual.

### Melhoria futura — mapas de cooldown de mensagens

Os cooldowns de mensagens de captura e comando usam mapas estáticos indexados
por jogador/aldeão. Eles não causam trabalho por tick, mas podem acumular
chaves em servidores ativos por muito tempo.

Proposta: limpar entradas do jogador no logout e podar entradas expiradas
quando a estrutura atingir um limite seguro. A mensagem continua limitada aos
mesmos 40 ticks; apenas a memória temporária deixa de crescer indefinidamente.

### Melhoria futura — consulta compartilhada de vila por jogador

Livro e missões consultam o totem efetivo no mesmo intervalo de 40 ticks.
Hoje isso é seguro e de baixa frequência. Só vale unificar a consulta após
medição em um servidor com muitos jogadores próximos de vilas especiais,
pois uma refatoração agora aumentaria o risco sem evidência de gargalo.

## Cenários manuais pendentes

- Abrir um Trader/Converter/Breeder/Incubator que contenha um aldeão capturado
  em versões antigas e confirmar que o aviso de item inválido não reaparece.
- Capturar e recolocar um Caçador com mão original vazia; confirmar que o
  forcado, as ofertas e a facção permanecem corretos.
- Fazer uma troca com Shift-click e fechar imediatamente a tela; confirmar
  que não há devolução de entrada nem duplicação.
- Reiniciar o mundo após cada cenário acima.

## Conclusão

A limpeza de NBT legado é compatível com os caminhos de snapshot auditados e
é a correção indicada para o aviso repetido. O build e os testes unitários
concluíram com sucesso; as duas melhorias adicionais devem permanecer
opcionais até aprovação explícita.
