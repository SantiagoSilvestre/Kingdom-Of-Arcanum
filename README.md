# Kingdom of Arcanum 🏰✨

**Kingdom of Arcanum** é um aplicativo companheiro para jogadores de RPG de mesa, desenvolvido em Android com as tecnologias mais modernas do ecossistema Google. O app permite gerenciar fichas de personagens de forma dinâmica, evoluir níveis e até dar vida aos seus heróis usando Inteligência Artificial.

## 🚀 Funcionalidades

### 🔐 Autenticação Segura
- Login convencional com E-mail e Senha.
- **Integração com Google Sign-In**: Entre rapidamente usando sua conta Google.
- Fluxo de Logout completo (Firebase + Google) para garantir a troca de contas.

### 🎭 Gestão de Personagens
- **Listagem Dinâmica**: Veja todos os seus heróis com avatares, raças, classes e níveis.
- **Ficha Completa**: 
    - Barras de status visuais para **Vida**, **Mana** e **XP**.
    - Cálculo automático: Vida = Força * 5 | Mana = Inteligência * 5.
    - Controle de Inventário (Itens) e Grimório (Habilidades).
- **Sistema de RPG**:
    - Gerenciamento de Ouro por modal interativo.
    - Uso de habilidades com consumo automático de Mana.
    - Travas de segurança para evitar valores negativos ou gastos indevidos.

### 📈 Evolução e Nível
- **Level Up**: Ganhe XP e suba de nível automaticamente (Nível 1: 100xp, Nível 2: 200xp, etc).
- **Pontos de Atributo**: Ganhe 1 ponto a cada nível para distribuir em Força ou Inteligência através de um modal exclusivo.

### 🎨 Criação de Imagem com IA
- **AI Portrait**: Gere imagens épicas do seu personagem apenas descrevendo-o.
- Integração com **Pollinations AI** (Stable Diffusion) adaptada com contexto de RPG automático.
- Visualização em tela cheia da arte gerada.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: [Kotlin](https://kotlinlang.org/)
- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Arquitetura**: MVVM (Model-View-ViewModel) com Clean Architecture principles.
- **Injeção de Dependência**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Backend**: [Firebase](https://firebase.google.com/)
    - Authentication (E-mail & Google)
    - Realtime Database (Sincronização em tempo real)
- **Carregamento de Imagem**: [Coil](https://coil-kt.github.io/coil/)
- **IA**: Pollinations AI API

---

## ⚙️ Configuração do Projeto

Para rodar este projeto localmente, você precisará configurar o Firebase:

1.  Crie um projeto no [Console do Firebase](https://console.firebase.google.com/).
2.  Adicione um app Android com o package `br.com.silvestresantiago732.kingdomofarcanum`.
3.  Baixe o arquivo `google-services.json` e coloque-o na pasta `app/`.
4.  No Firebase, ative:
    - **Authentication**: E-mail/Password e Google.
    - **Realtime Database**: Ative e configure as regras de leitura/escrita.
5.  **Web Client ID**: Copie o ID do cliente web no console do Firebase e cole no arquivo `strings.xml` do projeto:
    ```xml
    <string name="default_web_client_id">SEU_ID_AQUI.apps.googleusercontent.com</string>
    ```

---

## 📝 Licença

Este projeto é para fins de estudo e entretenimento. Sinta-se à vontade para expandir as regras para o seu próprio sistema de RPG!
