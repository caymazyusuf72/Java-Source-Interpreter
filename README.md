# Java Kaynak Kod Yorumlayıcısı (Java Source Interpreter)

## 📋 Proje Özeti

Bu proje, **Java kaynak kodunu bytecode (.class) üretmeden ve native binary derlemeden doğrudan yorumlayarak çalıştıran** bir Java uygulamasıdır.

### 🎯 Temel Konsept

```
Normal Java:    .java → javac → .class → JVM → Çalıştırma
Bizim Sistem:   .java → Lexer → Parser → AST → Interpreter → Çalıştırma
```

**Önemli:** Bu bir **meta-circular interpreter**'dır - Java'da yazılmış, Java'yı yorumlayan bir program.

## 🏗️ Mimari

### Katmanlar

```
┌─────────────────────────────────────┐
│   Kaynak Dosya (.java)              │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Lexer (Tokenization)              │
│   Kaynak kod → Token dizisi         │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Parser (Syntax Analysis)          │
│   Token'lar → AST                   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Interpreter (Tree Walking)        │
│   AST → Çalıştırma                  │
└──────────────┬──────────────────────┘
               │
               ▼
           Sonuç
```

## ✅ Desteklenen Özellikler

### Temel Özellikler
- ✅ Primitive tipler: `int`, `double`, `boolean`, `String`
- ✅ Değişken tanımlama ve atama
- ✅ Aritmetik operatörler: `+`, `-`, `*`, `/`, `%`
- ✅ Karşılaştırma: `==`, `!=`, `<`, `>`, `<=`, `>=`
- ✅ Mantıksal: `&&`, `||`, `!`
- ✅ Kontrol akışı: `if/else`, `while`, `for`
- ✅ `System.out.println()`

### OOP Özellikleri
- ✅ Sınıf tanımlama
- ✅ Metod tanımlama ve çağrı
- ✅ Parametreler ve return değerleri
- ✅ `new` ile nesne yaratma
- ✅ Alan (field) erişimi
- ✅ `this` referansı
- ✅ Constructor'lar

### ❌ Desteklenmeyen (v1.0)
- ❌ Kalıtım ve polymorphism
- ❌ Interface'ler
- ❌ Generic'ler
- ❌ Exception handling
- ❌ Static üyeler
- ❌ Array'ler
- ❌ Package sistemi

## 📁 Dosya Yapısı

```
java-interpreter/
├── README.md
├── plans/
│   ├── architecture.md           # Teknik mimari detayları
│   ├── implementation-guide.md   # Uygulama rehberi
│   └── execution-flow.md         # Çalıştırma akışı
├── src/
│   ├── Main.java
│   ├── lexer/
│   │   ├── Lexer.java
│   │   ├── Token.java
│   │   └── TokenType.java
│   ├── parser/
│   │   ├── Parser.java
│   │   └── ast/
│   │       ├── ASTNode.java
│   │       ├── Expression.java
│   │       ├── Statement.java
│   │       └── Declaration.java
│   ├── interpreter/
│   │   ├── Interpreter.java
│   │   ├── Environment.java
│   │   ├── Value.java
│   │   ├── JavaClass.java
│   │   └── JavaObject.java
│   └── util/
│       └── ErrorHandler.java
└── examples/
    ├── simple.java
    ├── calculator.java
    └── fibonacci.java
```

## 🚀 Kullanım

### Derleme
```bash
javac src/**/*.java -d out/
```

### Çalıştırma
```bash
java -cp out Main examples/simple.java
```

## 📝 Örnek Program

### Girdi: `examples/calculator.java`
```java
class Calculator {
    int add(int a, int b) {
        return a + b;
    }
}

class Main {
    void main() {
        Calculator calc = new Calculator();
        int result = calc.add(5, 3);
        System.out.println(result);
    }
}
```

### Çıktı
```
8
```

## 🔍 Nasıl Çalışır?

### 1. Lexical Analysis (Lexer)
Kaynak kodu token'lara ayırır:
```
"int x = 5;" → [INT, IDENTIFIER(x), EQUAL, NUMBER(5), SEMICOLON]
```

### 2. Syntax Analysis (Parser)
Token'ları AST'ye (Abstract Syntax Tree) dönüştürür:
```
VarDeclaration
├── type: INT
├── name: "x"
└── initializer: Literal(5)
```

### 3. Interpretation
AST'yi ziyaret ederek (visitor pattern) çalıştırır:
```java
visitVarDecl() → Environment'a "x = Value(INT, 5)" ekle
```

## ⚡ Performans

- **Normal Java (bytecode):** ~1ms
- **Bu Interpreter:** ~100-1000ms
- **Oran:** 100-1000x daha yavaş

### Neden Yavaş?
1. Her çalıştırmada AST traversal
2. Runtime tip kontrolü
3. Value boxing/unboxing maliyeti
4. Environment lookup overhead
5. Metod dispatch maliyeti

## 🎓 Eğitimsel Değer

Bu proje şunları öğretir:
- Compiler/interpreter tasarım prensipleri
- Lexical ve syntax analysis
- Abstract Syntax Tree yapıları
- Tree walking interpretation
- Runtime environment yönetimi
- Tip sistemleri
- Visitor pattern kullanımı

## 📚 Detaylı Dokümantasyon

- [`plans/architecture.md`](plans/architecture.md) - Teknik mimari ve teorik temel
- [`plans/implementation-guide.md`](plans/implementation-guide.md) - Adım adım uygulama rehberi
- [`plans/execution-flow.md`](plans/execution-flow.md) - Çalıştırma akışı ve örnekler

## 🔧 Geliştirme Sırası

1. ✅ Token & TokenType tanımlama
2. ✅ Lexer implementasyonu
3. ✅ AST node yapıları
4. ✅ Parser implementasyonu
5. ✅ Value ve tip sistemi
6. ✅ Environment ve scope yönetimi
7. ✅ Interpreter çekirdeği
8. ✅ Expression değerlendirici
9. ✅ Statement yürütücü
10. ✅ Metod çağrı mekanizması
11. ✅ Sınıf ve nesne desteği
12. ✅ Built-in fonksiyonlar
13. ✅ Hata yönetimi
14. ✅ Test senaryoları

## 🎯 Proje Hedefleri

### Başarı Kriterleri
- [x] Mimari ve planlama tamamlandı
- [ ] Lexer çalışıyor
- [ ] Parser AST oluşturuyor
- [ ] Basit ifadeler çalışıyor
- [ ] Değişkenler çalışıyor
- [ ] Kontrol akışı çalışıyor
- [ ] Metodlar çalışıyor
- [ ] Sınıflar ve nesneler çalışıyor
- [ ] Örnek programlar başarıyla çalışıyor

## 🤔 Neden Bu Proje?

### Öğrettiği Kavramlar
1. **Meta-programming:** Bir dilin kendisini yorumlama
2. **Compiler Design:** Lexer, Parser, Interpreter pipeline
3. **AST Manipulation:** Tree walking ve visitor pattern
4. **Runtime Systems:** Environment, scope, type systems
5. **Language Semantics:** Java'nın çalışma mantığı

### Gerçek Dünya Örnekleri
- Jython (Python in JVM)
- Groovy interpreter
- JavaScript engines
- Domain-specific language'lar

## 🚧 Bilinen Kısıtlamalar

1. **Performans:** Üretim kullanımı için uygun değil
2. **Özellik Seti:** Java'nın alt kümesi
3. **Array Desteği:** v1.0'da yok
4. **Exception Handling:** Desteklenmiyor
5. **Reflection:** Sınırlı
6. **Concurrency:** Thread desteği yok

## 📄 Lisans

Eğitim amaçlı proje.

## 👥 Katkıda Bulunma

Bu bir öğrenme projesidir. Katkılar memnuniyetle karşılanır!

---

**Not:** Bu interpreter, Java bytecode üretmeden, kaynak kodu doğrudan yorumlayarak çalıştırır. Bu yaklaşım eğitimsel amaçlıdır ve üretim kullanımı için tasarlanmamıştır.