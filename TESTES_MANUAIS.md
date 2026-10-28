# Lista de testes manuais — MCA × Vamp 1.3.1

Combinação preparada para Minecraft 1.20.1:

- Forge 47.4.20 / Java 17
- MCA 7.7.1-beta.5+1.20.1
- Vampirism 1.20.1-1.10.17
- Easy Villagers 1.20.1-1.1.39
- MCA × EV 1.1.9

Os testes automatizados e de inicialização em servidor foram concluídos conforme VALIDATION.md. Esta lista permanece para conferir o cliente gráfico e o seu próprio modpack.

Use uma cópia do seu mundo e mantenha apenas um JAR de cada mod na pasta mods. Faça estas verificações na combinação de versões indicada acima.

- [ ] Abrir o jogo, entrar no mundo e confirmar que os moradores antigos continuam presentes.
- [ ] Se usar servidor dedicado, iniciar o servidor e conectar com as mesmas versões no cliente. Conferir se não há erro de mixin ou de classe de interface.
- [ ] Capturar e soltar um morador com as mãos e com dispenser. Conferir nome, aparência, profissão, família, inventário e equipamento.
- [ ] Repetir a captura com um vampiro, um caçador e um lobisomem, se Werewolves estiver instalado. Conferir se a facção continua correta.
- [ ] Colocar moradores em Trader, Breeder, Converter e Incubator. Conferir aparência MCA e profissão no tooltip, inclusive com Shift.
- [ ] Salvar e reabrir o mundo com moradores dentro dos blocos e com um processo em andamento. Conferir moradores, itens e progresso.
- [ ] Tentar reproduzir um morador infértil: nenhum filho e nenhuma comida consumida. Depois testar dois pais férteis e conferir aparência, parentesco e herança do filho.
- [ ] Testar comércio com um caçador e um vampiro, incluindo permissões por facção, descontos e o botão para renovar trocas quando permitido.
- [ ] Curar um morador e conferir aparência, facção, itens e descontos de cura após retirá-lo do Converter.
- [ ] Abrir o Livro das Artes Ocultas: testar os dois marcadores, as setas, a numeração e textos compridos.
- [ ] Em uma vila controlada por totem, aceitar, acompanhar e entregar um favor. Reabrir o mundo com o pedido aceito e conferir que a recompensa só é entregue uma vez.
- [ ] Se usar proteção de família e amigos no combate, conferir o comportamento com a opção desligada e ligada, inclusive defesa após uma agressão.
- [ ] Olhar para vários blocos com moradores: observar FPS e conferir se o log repete erro de item inválido ou outra mensagem.
- [ ] Se usar Werewolves ou Bloodlines, repetir a abertura do mundo e as interações principais com esses mods. Também conferir uma instalação sem os opcionais.

Ao relatar um problema, anote as versões usadas, a ação que causou o erro e se ocorreu no cliente ou servidor. Guarde latest.log e o crash report, quando houver.
