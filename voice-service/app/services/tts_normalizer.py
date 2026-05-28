import re


TECHNICAL_TERM_REPLACEMENTS = {
    "StringBuilder": "стринг билдер",
    "StringBuffer": "стринг бафер",
    "ArrayList": "эррей лист",
    "LinkedList": "линкт лист",
    "HashMap": "хэш мап",
    "HashSet": "хэш сет",
    "TreeMap": "три мап",
    "TreeSet": "три сет",
    "ConcurrentHashMap": "конкаррент хэш мап",
    "Spring Security": "спринг секьюрити",
    "Spring Boot": "спринг бут",
    "Spring Data": "спринг дата",
    "Spring MVC": "спринг эм ви си",
    "Spring": "спринг",
    "Hibernate": "хайбернейт",
    "PostgreSQL": "постгрес кью эль",
    "Postgres": "постгрес",
    "JavaScript": "джава скрипт",
    "TypeScript": "тайп скрипт",
    "Node.js": "ноуд джей эс",
    "NodeJS": "ноуд джей эс",
    "React": "риакт",
    "Docker": "докер",
    "Kubernetes": "кубернетис",
    "immutable": "иммутабельные",
    "immutability": "иммутабельность",
    "mutable": "мутабельные",
    "String": "стринг",
    "Integer": "интеджер",
    "Long": "лонг",
    "Boolean": "булиан",
    "Object": "обджект",
    "Class": "класс",
    "interface": "интерфейс",
    "abstract": "абстрактный",
    "static": "статический",
    "final": "финальный",
    "volatile": "волатайл",
    "synchronized": "синхронайзд",
    "thread": "поток",
    "heap": "хип",
    "stack": "стек",
    "garbage collector": "гарбидж коллектор",
    "GC": "джи си",
    "JVM": "джей ви эм",
    "JDK": "джей ди кей",
    "JRE": "джей ар и",
    "JPA": "джей пи эй",
    "ORM": "о эр эм",
    "SQL": "эс кью эль",
    "NoSQL": "ноу эс кью эль",
    "REST": "рест",
    "GraphQL": "граф кью эль",
    "HTTP": "эйч ти ти пи",
    "HTTPS": "эйч ти ти пи эс",
    "API": "эй пи ай",
    "DTO": "ди ти о",
    "JSON": "джейсон",
    "XML": "экс эм эль",
    "JWT": "джей дабл ю ти",
    "OAuth2": "о аут два",
    "OAuth": "о аут",
    "CORS": "корс",
    "CRUD": "круд",
    "ACID": "эй сид",
    "CAP": "кап",
    "CI/CD": "си ай си ди",
    "Git": "гит",
    "GitHub": "гит хаб",
    "Linux": "линукс",
    "Redis": "редис",
    "Kafka": "кафка",
    "RabbitMQ": "рэббит эм кью",
    "MongoDB": "монго ди би",
    "MySQL": "май эс кью эль",
    "Nginx": "энджин икс",
}

ACRONYM_REPLACEMENTS = {
    "AI": "эй ай",
    "API": "эй пи ай",
    "CI": "си ай",
    "CLI": "си эл ай",
    "CPU": "си пи ю",
    "CSS": "си эс эс",
    "DB": "ди би",
    "DTO": "ди ти о",
    "GC": "джи си",
    "GPU": "джи пи ю",
    "HTML": "эйч ти эм эль",
    "HTTP": "эйч ти ти пи",
    "HTTPS": "эйч ти ти пи эс",
    "IO": "ай о",
    "JDK": "джей ди кей",
    "JPA": "джей пи эй",
    "JRE": "джей ар и",
    "JSON": "джейсон",
    "JVM": "джей ви эм",
    "JWT": "джей дабл ю ти",
    "MVC": "эм ви си",
    "ORM": "о эр эм",
    "REST": "рест",
    "SQL": "эс кью эль",
    "TDD": "ти ди ди",
    "UI": "ю ай",
    "URL": "ю ар эл",
    "UX": "ю икс",
    "XML": "экс эм эль",
}

CODE_COMPONENT_REPLACEMENTS = {
    "abstract": "абстракт",
    "adapter": "адаптер",
    "async": "эйсинк",
    "atomic": "атомик",
    "auth": "аут",
    "authentication": "аутентификация",
    "authorization": "авторизация",
    "batch": "батч",
    "bean": "бин",
    "boot": "бут",
    "boolean": "булиан",
    "builder": "билдер",
    "cache": "кэш",
    "class": "класс",
    "client": "клиент",
    "collection": "коллекшн",
    "completable": "комплитабл",
    "concurrent": "конкаррент",
    "config": "конфиг",
    "configuration": "конфигурация",
    "connection": "коннекшн",
    "consumer": "консьюмер",
    "context": "контекст",
    "controller": "контроллер",
    "database": "дейтабейс",
    "deadlock": "дедлок",
    "dependency": "депенденси",
    "docker": "докер",
    "entity": "энтити",
    "error": "эррор",
    "event": "ивент",
    "exception": "эксепшн",
    "executor": "экзекьютор",
    "factory": "фэктори",
    "filter": "фильтр",
    "final": "финальный",
    "framework": "фреймворк",
    "future": "фьючер",
    "gateway": "гейтвей",
    "handler": "хэндлер",
    "heap": "хип",
    "hibernate": "хайбернейт",
    "immutable": "иммутабельные",
    "implementation": "имплементация",
    "index": "индекс",
    "integer": "интеджер",
    "interface": "интерфейс",
    "java": "джава",
    "javascript": "джава скрипт",
    "kafka": "кафка",
    "kubernetes": "кубернетис",
    "lambda": "лямбда",
    "linked": "линкт",
    "listener": "листенер",
    "lock": "лок",
    "manager": "менеджер",
    "mapper": "маппер",
    "method": "метод",
    "microservice": "майкросервис",
    "mock": "мок",
    "mutable": "мутабельные",
    "node": "ноуд",
    "object": "обджект",
    "pool": "пул",
    "postgres": "постгрес",
    "postgresql": "постгрес кью эль",
    "producer": "продьюсер",
    "production": "продакшене",
    "provider": "провайдер",
    "queue": "кью",
    "rabbit": "рэббит",
    "react": "риакт",
    "redis": "редис",
    "repository": "репозиторий",
    "request": "реквест",
    "response": "респонс",
    "retry": "ретрай",
    "security": "секьюрити",
    "server": "сервер",
    "service": "сервис",
    "spring": "спринг",
    "stack": "стек",
    "static": "статический",
    "stream": "стрим",
    "string": "стринг",
    "sync": "синк",
    "synchronized": "синхронайзд",
    "thread": "тред",
    "timeout": "таймаут",
    "token": "токен",
    "transaction": "транзакция",
    "typescript": "тайп скрипт",
    "user": "юзер",
    "validator": "валидатор",
    "virtual": "виртуальный",
    "volatile": "волатайл",
    "worker": "воркер",
}

LATIN_TOKEN_PATTERN = re.compile(r"(?<![A-Za-zА-Яа-яЁё])([A-Za-z][A-Za-z0-9_./-]*)(?![A-Za-zА-Яа-яЁё])")


def normalize_text_for_tts(text: str) -> str:
    normalized = strip_markdown_code(text)
    for term, replacement in sorted(
        TECHNICAL_TERM_REPLACEMENTS.items(), key=lambda item: len(item[0]), reverse=True
    ):
        normalized = replace_term(normalized, term, replacement)
    return LATIN_TOKEN_PATTERN.sub(lambda match: pronounce_latin_token(match.group(1)), normalized)


def strip_markdown_code(text: str) -> str:
    return re.sub(r"`([^`]+)`", r"\1", text)


def replace_term(text: str, term: str, replacement: str) -> str:
    if term.isalpha() or term.replace(" ", "").isalpha():
        pattern = rf"(?<![A-Za-zА-Яа-яЁё]){re.escape(term)}(?![A-Za-zА-Яа-яЁё])"
    else:
        pattern = rf"(?<![A-Za-zА-Яа-яЁё0-9]){re.escape(term)}(?![A-Za-zА-Яа-яЁё0-9])"
    return re.sub(pattern, replacement, text, flags=re.IGNORECASE)


def pronounce_latin_token(token: str) -> str:
    acronym = token.upper()
    if acronym in ACRONYM_REPLACEMENTS:
        return ACRONYM_REPLACEMENTS[acronym]

    parts = split_code_token(token)
    pronounced_parts = [pronounce_code_part(part) for part in parts]
    return " ".join(part for part in pronounced_parts if part)


def split_code_token(token: str) -> list[str]:
    token = re.sub(r"[_.\-/]+", " ", token)
    token = re.sub(r"([A-Z]+)([A-Z][a-z])", r"\1 \2", token)
    token = re.sub(r"([a-z0-9])([A-Z])", r"\1 \2", token)
    return [part for part in token.split() if part]


def pronounce_code_part(part: str) -> str:
    acronym = part.upper()
    if acronym in ACRONYM_REPLACEMENTS:
        return ACRONYM_REPLACEMENTS[acronym]

    replacement = CODE_COMPONENT_REPLACEMENTS.get(part.lower())
    if replacement is not None:
        return replacement

    if part.isupper() and len(part) <= 5:
        return " ".join(pronounce_letter(letter) for letter in part)

    return part.lower()


def pronounce_letter(letter: str) -> str:
    return {
        "A": "эй",
        "B": "би",
        "C": "си",
        "D": "ди",
        "E": "и",
        "F": "эф",
        "G": "джи",
        "H": "эйч",
        "I": "ай",
        "J": "джей",
        "K": "кей",
        "L": "эл",
        "M": "эм",
        "N": "эн",
        "O": "о",
        "P": "пи",
        "Q": "кью",
        "R": "ар",
        "S": "эс",
        "T": "ти",
        "U": "ю",
        "V": "ви",
        "W": "дабл ю",
        "X": "икс",
        "Y": "вай",
        "Z": "зет",
    }.get(letter.upper(), letter.lower())
