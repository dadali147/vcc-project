# Page snapshot

```yaml
- generic [ref=e8]:
  - generic [ref=e9]:
    - img "kimoox" [ref=e10]
    - heading "Login" [level=1] [ref=e11]
    - paragraph [ref=e12]: 虚拟信用卡商户管理系统
  - generic [ref=e13]:
    - generic [ref=e14]:
      - generic [ref=e15]: Email
      - textbox "Email" [active] [ref=e16]: merchant
    - generic [ref=e17]:
      - generic [ref=e18]: Password
      - textbox "Password" [ref=e19]: wrongpassword
    - generic [ref=e20]:
      - generic [ref=e21] [cursor=pointer]:
        - checkbox "Remember Me" [ref=e22]
        - text: Remember Me
      - link "Forgot Password?" [ref=e23] [cursor=pointer]:
        - /url: "#"
    - button "Login" [ref=e24] [cursor=pointer]:
      - generic [ref=e25]: Login
  - paragraph [ref=e27]:
    - text: No account?
    - link "Register" [ref=e28] [cursor=pointer]:
      - /url: /register
```