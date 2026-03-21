# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - generic [ref=e8]:
    - generic [ref=e9]:
      - img "kimoox" [ref=e10]
      - heading "Login" [level=1] [ref=e11]
      - paragraph [ref=e12]: 虚拟信用卡商户管理系统
    - generic [ref=e13]:
      - generic [ref=e14]:
        - generic [ref=e15]: Email
        - textbox "Email" [ref=e16]: nonexistent@example.com
      - generic [ref=e17]:
        - generic [ref=e18]: Password
        - textbox "Password" [ref=e19]: admin123
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
    - generic [ref=e29]:
      - img [ref=e30]
      - text: 登录失败，请检查您的邮箱和密码。
  - alert [ref=e32]:
    - img [ref=e34]
    - paragraph [ref=e36]: 登录失败，请检查您的邮箱和密码。
```