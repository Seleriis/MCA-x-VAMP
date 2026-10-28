# Relatório de testes — 28/10/2026

A validação de 28/10/2026 utilizou ambientes descartáveis e os JARs de distribuição das versões 1.3.1 e 2.3.1. Não foi necessário alterar o código nem substituir os JARs.

| Verificação | Forge 1.20.1 / Vamp 1.3.1 | NeoForge 1.21.1 / Vamp 2.3.1 |
|---|---|---|
| Compilação e JUnit, em 27/10 | 85 aprovados, 10 ignorados, 0 falhas | 97 aprovados, 0 falhas |
| GameTests com perfil all | 37 aprovados em 27/10 | 32 aprovados em 28/10 |
| Inicialização normal com JAR de produção, em 28/10 | Aprovada; mensagem Done | Aprovada; mensagem Done |
| Criar morador MCA, inspect, resync e salvar | Aprovados | Aprovados |
| Encerramento por stop | Código 0 | Código 0 |
| Contrato de mixins/refmap Forge contra Minecraft SRG | Aprovado | Não se aplica a esse contrato Forge |

## Combinações utilizadas

- 1.20.1: Java 17, Forge 47.4.20, MCA 7.7.1-beta.5+1.20.1, Vampirism 1.10.17, Easy Villagers 1.20.1-1.1.39, MCA × EV 1.1.9 e Werewolves 2.0.2.7.
- 1.21.1: Java 21, NeoForge 21.1.234, MCA 7.7.36-beta.3+1.21.1, Vampirism 1.10.13, Easy Villagers 1.21.1-1.1.42, MCA × EV 2.2.7, Werewolves 2.0.3.3 e Bloodlines 3.0.9.

A pendência da inicialização Forge anterior foi resolvida no novo ensaio: o servidor iniciou e encerrou normalmente. O ensaio anterior permanece arquivado; sua causa não foi determinada. A verificação do refmap foi aplicada a um build cujas 334 entradas têm conteúdo idêntico ao JAR 1.3.1 entregue.

## JARs testados

- mca-vamp-compat-1.20.1-1.3.1.jar: 8ae9ed0731f787f17b5b6f8ef535e04c5e606a548186d11dc33755ba72fe47bc
- mca-vamp-compat-1.21.1-2.3.1.jar: 71ca6204bdd32e2e65fcd12f34e65422c17ffff47e1f77683cced8cc46793e54

## Evidências

- 1.20.1/testing/1.3.1/2026-10-28/: production-logs, production-contract.log, artifact-contract-comparison.json e run.json.
- 1.21.1/testing/2.3.1/2026-10-28/: gametest.log, gametest-runtime-logs, production-logs, run.json, production-run.json e server-install.log.
- shared/reports/2026-10-28/log-review.json: nenhuma ocorrência de nível ERROR/FATAL nem dos erros de ligação pesquisados nos três logs de runtime revisados.

Os logs contêm avisos de bibliotecas, configuração inicial e integrações opcionais, incluindo avisos do Werewolves sobre categorias de spawn; eles não impediram as execuções descritas.

## Verificações manuais restantes

Aparência dos moradores, skins, interfaces, escalas de GUI, shaders e FPS não foram avaliados em cliente gráfico. Também não foi repetida toda a matriz histórica de versões ou uma migração de um mundo pessoal. A lista TESTES_MANUAIS.md permanece nos pacotes para essas verificações e para o seu modpack.

Nenhum mundo pessoal foi aberto. Estes resultados acompanham os pré-lançamentos v1.3.1-1.20.1 e v2.3.1-1.21.1 no GitHub.
